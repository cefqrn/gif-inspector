package gif.block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import gif.data.GlobalColorTable;
import gif.data.Serializable;
import gif.data.Unsigned;
import gif.data.Version;
import gif.data.exception.InvalidValue;
import gif.data.exception.OutOfBounds;
import gif.data.exception.ParseException;
import gif.data.exception.UnexpectedEndOfStream;

public sealed interface Block extends Serializable permits LabeledBlock, Block.Header, Block.Screen {
  record Header(List<Unsigned.Byte> signature, Version version) implements Block {
    private static final List<Unsigned.Byte> EXPECTED_SIGNATURE = Collections.unmodifiableList(Unsigned.Byte.listFrom("GIF".getBytes()));

    public Header(List<Unsigned.Byte> signature, Version version) {
      InvalidValue.check("signature", signature, EXPECTED_SIGNATURE);

      this.signature = EXPECTED_SIGNATURE;
      this.version   = Objects.requireNonNull(version);
    }

    public static Header readFrom(InputStream stream) throws IOException, ParseException {
      var signature = Unsigned.Byte.listFrom(stream.readNBytes(3));
      var version   = Version.readFrom(stream);

      return new Header(signature, version);
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
      for (var b : signature)
        b.writeTo(stream);

      version.writeTo(stream);
    }
  }

  record Screen(
    Unsigned.Short width,
    Unsigned.Short height,
    Unsigned.Byte pixelAspectRatio,
    int colorResolution,
    Optional<GlobalColorTable> globalColorTable
  ) implements Block {
    public Screen(Unsigned.Short width, Unsigned.Short height, Unsigned.Byte pixelAspectRatio, int colorResolution, Optional<GlobalColorTable> globalColorTable) {
      this.width            = Objects.requireNonNull(width );
      this.height           = Objects.requireNonNull(height);
      this.pixelAspectRatio = Objects.requireNonNull(pixelAspectRatio);
      this.colorResolution  = OutOfBounds.check("color resolution", colorResolution, 0, 7);
      this.globalColorTable = globalColorTable.map(Objects::requireNonNull);
    }

    public static Screen readFrom(InputStream stream) throws IOException, UnexpectedEndOfStream {
      var width  = Unsigned.Short.readFrom(stream);
      var height = Unsigned.Short.readFrom(stream);

      var packedFields = Unsigned.Byte.readFrom(stream).intValue();

      var backgroundColorIndex = Unsigned.Byte.readFrom(stream);
      var pixelAspectRatio     = Unsigned.Byte.readFrom(stream);

      var hasGlobalColorTable =  (packedFields >> 7) == 1;
      var colorResolution     =  (packedFields >> 4) & 7;

      var isSorted            = ((packedFields >> 3) & 1) == 1;
      var packedSize          =  (packedFields >> 0) & 7;
      Optional<GlobalColorTable> globalColorTable = Optional.empty();
      if (hasGlobalColorTable) {
        globalColorTable = Optional.of(GlobalColorTable.readFrom(stream, packedSize, isSorted, backgroundColorIndex));
      } else {
        if (!backgroundColorIndex.equals(Unsigned.Byte.ZERO))
          System.err.println("WARNING: missing global color table, but background color index specified");

        if (isSorted)
          System.err.println("WARNING: missing global color table marked as sorted (not wrong but still)");

        /*
        *   "Even if there is no Global Color Table specified, set this
        *    field [...] so that decoders can choose
        *    the best graphics mode to display the stream in."
        *                                                     - gif89a spec (18)
        *  nonzero packed size might be valid if the decoder has a color table loaded in (11)
        *  but that could go into another constructor where the table is passed in
        *  alternatively the global color table could be moved out of Screen
        */
        if (packedSize != 0)
          System.err.println("WARNING: missing global color table has nonzero packed size");
      }

      return new Screen(width, height, pixelAspectRatio, colorResolution, globalColorTable);
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
      width .writeTo(stream);
      height.writeTo(stream);

      var packedFields =   (colorResolution      << 4    )
                      | globalColorTable.map(GlobalColorTable::colorTable).map(table
                        -> (                   1 << 7    )  // has global color table
                         | (table.isSorted() ? 1 << 3 : 0)
                         | (table.packedSize()   << 0    )).orElse(0);
      stream.write(packedFields);

      globalColorTable.map(GlobalColorTable::backgroundColorIndex).orElse(Unsigned.Byte.ZERO).writeTo(stream);
      pixelAspectRatio.writeTo(stream);

      // can't use ifPresent since writeTo throws
      if (globalColorTable.isPresent())
        globalColorTable.get().colorTable().writeTo(stream);
    }
  }
}
