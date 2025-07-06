package gif.block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gif.data.Color;
import gif.data.exception.UnexpectedEndOfStream;
import gif.module.Read;
import gif.module.Write;

public class Screen extends Block {
  public final int width;
  public final int height;
  public final int pixelAspectRatio;
  public final int colorResolution;
  public final Optional<List<Color>> globalColorTable;
  public final boolean globalColorTableIsSorted;
  public final int backgroundColorIndex;

  public Screen(InputStream stream) throws IOException, UnexpectedEndOfStream {
    width = Read.U16From(stream);
    height = Read.U16From(stream);

    var packedFields = Read.U8From(stream);

    backgroundColorIndex = Read.U8From(stream);
    pixelAspectRatio = Read.U8From(stream);

    var hasGlobalColorTable  =        (packedFields >> 7) == 1;
    colorResolution          =        (packedFields >> 4) & 7;
    globalColorTableIsSorted =       ((packedFields >> 3) & 1) == 1;

    List<Color> table = null;
    if (hasGlobalColorTable) {
      var size               = 1 << (((packedFields >> 0) & 7) + 1);

      table = new ArrayList<>(size);
      for (var i=0; i < size; ++i)
        table.add(new Color(stream));
    }
    globalColorTable = Optional.ofNullable(table);
  }

  @Override
  public void writeTo(OutputStream stream) throws IOException {
    Write.U16To(stream, width);
    Write.U16To(stream, height);

    var packedFields =    (colorResolution << 4)
                     | globalColorTable.map(table -> {
      int packedSize = 0;
      for (var left=table.size(); left > 2; left >>= 1)
        packedSize++;

      return                            (1 << 7)      // has global color table
           | (globalColorTableIsSorted ? 1 << 3 : 0)
           |                   (packedSize << 0);
    }).orElse(0);
    Write.U8To(stream, packedFields);

    Write.U8To(stream, backgroundColorIndex);
    Write.U8To(stream, pixelAspectRatio);

    // can't use ifPresent since Color.writeTo throws
    if (globalColorTable.isPresent()) {
      for (var color : globalColorTable.get())
        color.writeTo(stream);
    }
  }
}
