package ibraim.opengles2;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TriangleColorActivity extends Activity
{
  private GLSurfaceView surface;
  private TriangleColorRenderer renderer;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // If you don't know what we're doing here, take a look at the
    // epilepsy sample.
    surface = new GLSurfaceView(this);
    renderer = new TriangleColorRenderer();
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

  private class TriangleColorRenderer implements GLSurfaceView.Renderer
  {
    private int vertexHandle;
    private int fragmentHandle;
    private int programHandle = -1;

    // These two methods help to Load/Unload the shaders used by OpenGL Es 2.0
    // Remember that now OpenGL DOES NOT CONTAIN most of the 'old' OpenGL functions;
    // Now you need to create your own vertex and fragment shaders. Yay!
    public void setup()
    {
      // make sure there's nothing already created
      tearDown();

      // Vertex shader source.
      // This is the the same one used in the Triangle2d sample, but with
      // an extra attribute: aColor, that will hold a RGB value for the color
      // of the vertex. This value will be passed directly to vColor
      String vertexSrc =
        "uniform mat4 uScreen;\n" +
        "attribute vec2 aPosition;\n" +
        "attribute vec3 aColor;\n" +
        "varying vec3 vColor;\n" +
        "void main() {\n" +
        "  gl_Position = uScreen * vec4(aPosition.xy, 0.0, 1.0);\n" +
        "  vColor = aColor;\n" +
        "}";

      // Our fragment shader. Just return vColor.
      // If you look at this source and just said 'WTF?', remember
      // that all the attributes are defined in the VERTEX shader and
      // all the 'varying' vars are considered OUTPUT of vertex shader
      // and INPUT of the fragment shader. Here we just use the color
      // we received and add a alpha value of 1.
      String fragmentSrc =
        "precision mediump float;\n"+
        "varying vec3 vColor;\n" +
        "void main(void)\n" +
        "{\n" +
        "  gl_FragColor = vec4(vColor.xyz, 1);\n" +
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
      // as usual, nothing to do here.
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
      // lets initialize everything
      setup();

      // discover the 'position' of the uScreen value
      int uScreenPos = GLES20.glGetUniformLocation(programHandle, "uScreen");

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

      // set the viewport and a fixed, white background
      GLES20.glViewport(0, 0, width, height);
      GLES20.glClearColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
      // get the position of our attributes
      int aPosition = GLES20.glGetAttribLocation(programHandle, "aPosition");
      int aColor = GLES20.glGetAttribLocation(programHandle, "aColor");

      // The triangle vertices. Note how I'm putting the
      // vertex position and the color on the same array.
      // This ensures maximum performance for this kind of operation.
      float[] data =
      {
        // XY, RGB
        50f, 100f,
        1f, 0f, 0f,

        300f, 100f,
        0f, 1f, 0f,

        200f, 170f,
        0f, 0f, 1f
      };

      // Now we will declare some constants just to make things easier to
      // understand. The values should be obvious if you look at the data array
      final int FLOAT_SIZE = 4;
      final int POSITION_SIZE = 2;
      final int COLOR_SIZE = 3;
      final int TOTAL_SIZE = POSITION_SIZE + COLOR_SIZE;
      final int POSITION_OFFSET = 0; // the start of the first position
      final int COLOR_OFFSET = 2;    // the start of the first color

      // Again, a FloatBuffer will be used to pass the values
      FloatBuffer b = ByteBuffer.allocateDirect(data.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
      b.put(data);

      // Enable and set the vertex attribute to accept our array.
      // This will set ONLY the positions
      b.position(POSITION_OFFSET);
      GLES20.glVertexAttribPointer(aPosition, POSITION_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
      GLES20.glEnableVertexAttribArray(aPosition);

      // Now, we do the same thing for the color attribute
      b.position(COLOR_OFFSET);
      GLES20.glVertexAttribPointer(aColor, COLOR_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
      GLES20.glEnableVertexAttribArray(aColor);

      // Clear the screen and draw the triangle
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
  }
}
