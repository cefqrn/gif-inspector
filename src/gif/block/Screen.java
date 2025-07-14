package gif.block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;

import gif.data.ColorTable;
import gif.data.exception.OutOfBounds;
import gif.data.exception.UnexpectedEndOfStream;
import gif.module.Read;
import gif.module.Write;

public record Screen(
  int width,
  int height,
  int pixelAspectRatio,
  int colorResolution,
  Optional<GlobalColorTable> globalColorTable
) implements Block {
  public Screen(int width, int height, int pixelAspectRatio, int colorResolution, Optional<GlobalColorTable> globalColorTable) {
    this.width            = OutOfBounds.check("width",  width,  0, 0xffff);
    this.height           = OutOfBounds.check("height", height, 0, 0xffff);
    this.pixelAspectRatio = OutOfBounds.check("pixel aspect ratio", pixelAspectRatio, 0, 0xff);
    this.colorResolution  = OutOfBounds.check("color resolution", colorResolution, 0, 7);
    this.globalColorTable = globalColorTable.map(Objects::requireNonNull);
  }

  public static Screen readFrom(InputStream stream) throws IOException, UnexpectedEndOfStream {
    var width  = Read.U16From(stream);
    var height = Read.U16From(stream);

    var packedFields = Read.U8From(stream);

    var backgroundColorIndex = Read.U8From(stream);
    var pixelAspectRatio     = Read.U8From(stream);

    var hasGlobalColorTable =  (packedFields >> 7) == 1;
    var colorResolution     =  (packedFields >> 4) & 7;

    var isSorted            = ((packedFields >> 3) & 1) == 1;
    var packedSize          =  (packedFields >> 0) & 7;
    Optional<GlobalColorTable> globalColorTable = Optional.empty();
    if (hasGlobalColorTable) {
      globalColorTable = Optional.of(GlobalColorTable.readFrom(stream, packedSize, isSorted, backgroundColorIndex));
    } else {
      if (backgroundColorIndex != 0)
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
    Write.U16To(stream, width);
    Write.U16To(stream, height);

    var packedFields =    (colorResolution      << 4    )
                     | globalColorTable.map(GlobalColorTable::colorTable).map(table
                       -> (                   1 << 7    )  // has global color table
                        | (table.isSorted() ? 1 << 3 : 0)
                        | (table.packedSize()   << 0    )).orElse(0);
    Write.U8To(stream, packedFields);

    Write.U8To(stream, globalColorTable.map(GlobalColorTable::backgroundColorIndex).orElse(0));
    Write.U8To(stream, pixelAspectRatio);

    // can't use ifPresent since writeTo throws
    if (globalColorTable.isPresent())
      globalColorTable.get().colorTable().writeTo(stream);
  }

  public record GlobalColorTable(ColorTable colorTable, int backgroundColorIndex) {
    public GlobalColorTable(ColorTable colorTable, int backgroundColorIndex) {
      this.colorTable           = Objects.requireNonNull(colorTable);
      this.backgroundColorIndex = OutOfBounds.check("background color index", backgroundColorIndex, 0, colorTable.colors().size() - 1);
    }

    public static GlobalColorTable readFrom(InputStream stream, int packedSize, boolean isSorted, int backgroundColorIndex) throws IOException, UnexpectedEndOfStream {
      return new GlobalColorTable(ColorTable.readFrom(stream, packedSize, isSorted), backgroundColorIndex);
    }
  }
}
