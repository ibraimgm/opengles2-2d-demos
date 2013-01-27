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

public class Triangle2dActivity extends Activity
{
  private GLSurfaceView surface;
  private Triangle2dRenderer renderer;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // If you don't know what we're doing here, take a look at the
    // epilepsy sample.
    surface = new GLSurfaceView(this);
    renderer = new Triangle2dRenderer();
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

  private class Triangle2dRenderer implements GLSurfaceView.Renderer
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
      // This shader uses a constant 4x4 matrix 'uScreen' and multiplies it to
      // the parameter aPosition. The x and y values of aPosition will be filled with
      // the vertices of our triangle. uScreen will be a matrix that, when multiplied
      // with the values of our position will CONVERT these values to the OpenGL coordinate
      // system. This way we can, say, inform our coordinates in 'pixels' and let OpenGL
      // figure out were the hell the pixels are. More on this later.
      String vertexSrc =
        "uniform mat4 uScreen;\n" +
        "attribute vec2 aPosition;\n" +
        "void main() {\n" +
        "  gl_Position = uScreen * vec4(aPosition.xy, 0.0, 1.0);\n" +
        "}";

      // Our fragment shader. Always returns the color RED.
      String fragmentSrc =
        "precision mediump float;\n"+
        "void main(void)\n" +
        "{\n" +
        "  gl_FragColor = vec4(1, 0, 0, 1);\n" +
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
      // Let's stop  for a minute and think on what we're doing here.
      //
      // First of all, the only coordinate system that OpenGL understands
      // put the center of the screen at the 0,0 position. The maximum value of
      // the X axis is 1 (rightmost part of the screen) and the minimum is -1
      // (leftmost part of the screen). The same thing goes for the Y axis,
      // where 1 is the top of the screen and -1 the bottom.
      //
      // However, when you're doing a 2d application you often need to think in 'pixels'
      // (or something like that). If you have a 300x300 screen, you want to see the center
      // at 150,150 not 0,0!
      //
      // The solution to this 'problem' is to multiply a matrix with your position to
      // another matrix that will convert 'your' coordinates to the one OpenGL expects.
      // There's no magic in this, only a bit of math. Try to multiply the uScreen matrix
      // to the 150,150 position in a sheet of paper and look at the results.
      //
      // IMPORTANT: When trying to calculate the matrix on paper, you should treat the
      // uScreen ROWS as COLUMNS and vice versa. This happens because OpenGL expect the
      // matrix values ordered in a more efficient way, that unfortunately is different
      // from the mathematical notation :(
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
      // get the position of 'aPosition'
      int aPos = GLES20.glGetAttribLocation(programHandle, "aPosition");

      // The triangle vertices. Note how I'm using
      // a 'pixel' coordinate system. This is not in the center of the
      // screen or anything; this is in absolute position, will vary depending
      // on the size of your display.
      float[] data =
      {
        50f, 100f,
        300f, 100f,
        200f, 170f,
      };

      // Again, a FloatBuffer will be used to pass the values
      FloatBuffer b = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      b.put(data).position(0);

      // Enable and set the vertex attribute to accept our array.
      // This makes possible to inform all of the vertices in one call.
      GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, b);
      GLES20.glEnableVertexAttribArray(aPos);

      // Clear the screen and draw the triangle
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
  }
}
