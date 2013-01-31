package ibraim.opengles2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextureActivity extends Activity
{
  private GLSurfaceView surface;
  private TextureRenderer renderer;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // If you don't know what we're doing here, take a look at the
    // epilepsy sample.
    surface = new GLSurfaceView(this);
    renderer = new TextureRenderer();
    surface.setEGLContextClientVersion(2);
    surface.setRenderer(renderer);

    setContentView(surface);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    surface.onResume();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    surface.onPause();
    renderer.tearDown();
  }

  private class TextureRenderer implements GLSurfaceView.Renderer
  {
    private int vertexHandle;
    private int fragmentHandle;
    private int programHandle = -1;
    private int[] textures = new int[1];

    // These two methods help to Load/Unload the shaders used by OpenGL Es 2.0
    // Remember that now OpenGL DOES NOT CONTAIN most of the 'old' OpenGL functions;
    // Now you need to create your own vertex and fragment shaders. Yay!
    public void setup()
    {
      // make sure there's nothing already created
      tearDown();

      // Vertex shader source.
      // Now things start to get interesting. Take note of a new attribute,
      // aTexPos, that will store the texture coordinate (the "places" of the texture that
      // we will use. We also have vTexPos, to pass the attribute value to the
      // fragment shader.
      String vertexSrc =
        "uniform mat4 uScreen;\n" +
        "attribute vec2 aPosition;\n" +
        "attribute vec2 aTexPos;\n" +
        "varying vec2 vTexPos;\n" +
        "void main() {\n" +
        "  vTexPos = aTexPos;\n" +
        "  gl_Position = uScreen * vec4(aPosition.xy, 0.0, 1.0);\n" +
        "}";

      // Our fragment shader.
      // Here we have a uniform (uTexture) that will hold the texture
      // for drawing. The 'color' of the vertex is calculated using the
      // texture coordinate (vTexPos) and the texture itself.
      String fragmentSrc =
        "precision mediump float;\n"+
        "uniform sampler2D uTexture;\n" +
        "varying vec2 vTexPos;\n" +
        "void main(void)\n" +
        "{\n" +
        "  gl_FragColor = texture2D(uTexture, vTexPos);\n" +
        "}";

      // Lets load and compile our shaders, link the program
      // and tell OpenGL ES to use it for future drawing.
      vertexHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexSrc);
      fragmentHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc);
      programHandle = createProgram(vertexHandle, fragmentHandle);

      GLES20.glUseProgram(programHandle);
    }

    public void tearDown()
    {
      if (programHandle != -1)
      {
        GLES20.glDeleteProgram(programHandle);
        GLES20.glDeleteShader(vertexHandle);
        GLES20.glDeleteShader(fragmentHandle);
        GLES20.glDeleteTextures(textures.length, textures, 0); // free the texture!
      }
    }

    // auxiliary shader functions. Doesn't matter WHAT you're trying to do, they're
    // always the same thing.
    private int loadShader(int shaderType, String shaderSource)
    {
      int handle = GLES20.glCreateShader(shaderType);

      if (handle == GLES20.GL_FALSE)
        throw new RuntimeException("Error creating shader!");

      // set and compile the shader
      GLES20.glShaderSource(handle, shaderSource);
      GLES20.glCompileShader(handle);

      // check if the compilation was OK
      int[] compileStatus = new int[1];
      GLES20.glGetShaderiv(handle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

      if (compileStatus[0] == 0)
      {
        String error = GLES20.glGetShaderInfoLog(handle);
        GLES20.glDeleteShader(handle);
        throw new RuntimeException("Error compiling shader: " + error);
      }
      else
        return handle;
    }

    private int createProgram(int vertexShader, int fragmentShader)
    {
      int handle = GLES20.glCreateProgram();

      if (handle == GLES20.GL_FALSE)
        throw new RuntimeException("Error creating program!");

      // attach the shaders and link the program
      GLES20.glAttachShader(handle, vertexShader);
      GLES20.glAttachShader(handle, fragmentShader);
      GLES20.glLinkProgram(handle);

      // check if the link was successful
      int[] linkStatus = new int[1];
      GLES20.glGetProgramiv(handle, GLES20.GL_LINK_STATUS, linkStatus, 0);

      if (linkStatus[0] == 0)
      {
        String error = GLES20.glGetProgramInfoLog(handle);
        GLES20.glDeleteProgram(handle);
        throw new RuntimeException("Error in program linking: " + error);
      }
      else
        return handle;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
      // first, try to generate a texture handle
      GLES20.glGenTextures(1, textures, 0);

      if (textures[0] == GLES20.GL_FALSE)
        throw new RuntimeException("Error loading texture");

      // bind the texture and set parameters
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

      // Load a bitmap from resources folder and pass it to OpenGL
      // in the end, we recycle it to free unneeded resources
      Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.skull);
      GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0);
      b.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
      // lets initialize everything
      setup();

      // discover the 'position' of the uScreen and uTexture
      int uScreenPos = GLES20.glGetUniformLocation(programHandle, "uScreen");
      int uTexture = GLES20.glGetUniformLocation(programHandle, "uTexture");

      // The uScreen matrix
      // This is explained in detail in the Triangle2d sample.
      float[] uScreen =
      {
         2f/width,   0f,         0f, 0f,
         0f,        -2f/height,  0f, 0f,
         0f,         0f,         0f, 0f,
        -1f,         1f,         0f, 1f
      };

      // Now, let's set the value.
      FloatBuffer b = ByteBuffer.allocateDirect(uScreen.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      b.put(uScreen).position(0);
      GLES20.glUniformMatrix4fv(uScreenPos, b.limit() / uScreen.length, false, b);

      // Activate the first texture (GL_TEXTURE0) and bind it to our handle
      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
      GLES20.glUniform1i(uTexture, 0);

      // set the viewport and a fixed, white background
      GLES20.glViewport(0, 0, width, height);
      GLES20.glClearColor(1f, 1f, 1f, 1f);

      // since we're using a PNG file with transparency, enable alpha blending.
      GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
      GLES20.glEnable(GLES20.GL_BLEND);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
      // get the position of our attributes
      int aPosition = GLES20.glGetAttribLocation(programHandle, "aPosition");
      int aTexPos = GLES20.glGetAttribLocation(programHandle, "aTexPos");

      // Ok, now is the FUN part.
      // First of all, our image is a rectangle right? but in OpenGL, we can only draw
      // triangles! To remedy that we will use 4 vertices (V1 to V4) and draw using
      // the TRIANGLE_STRIP option. If you look closely to our positions, you will note
      // that we're drawing a 'N' (or 'Z') shaped line... and TRIANGLE_STRIP 'closes' the
      // remaining GAP between the vertices, so we have a rectangle (or square)! Yay!
      //
      // Apart from V1 to V4, we also specify the position IN THE TEXTURE. Each vertex
      // of our rectangle must relate to a position in the texture. The texture coordinates
      // are ALWAYS 0,0 on bottom-left and 1,1 on top-right. Take a look at the values
      // used and you will understand it easily. If not, mess a little bit with the values
      // and take a look at the result.
      float[] data =
      {
        50f, 100f,  //V1
        0f, 0f,     //Texture coordinate for V1

        50f, 300f,  //V2
        0f,  1f,

        300f, 100f, //V3
        1f, 0f,

        300f, 300f,  //V4
        1f, 1f
      };

      // constants. You know the drill by now.
      final int FLOAT_SIZE = 4;
      final int POSITION_SIZE = 2;
      final int TEXTURE_SIZE = 2;
      final int TOTAL_SIZE = POSITION_SIZE + TEXTURE_SIZE;
      final int POSITION_OFFSET = 0;
      final int TEXTURE_OFFSET = 2;

      // Again, a FloatBuffer will be used to pass the values
      FloatBuffer b = ByteBuffer.allocateDirect(data.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
      b.put(data);

      // Position of our image
      b.position(POSITION_OFFSET);
      GLES20.glVertexAttribPointer(aPosition, POSITION_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
      GLES20.glEnableVertexAttribArray(aPosition);

      // Positions of the texture
      b.position(TEXTURE_OFFSET);
      GLES20.glVertexAttribPointer(aTexPos, TEXTURE_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
      GLES20.glEnableVertexAttribArray(aTexPos);

      // Clear the screen and draw the rectangle
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
  }
}
