package ibraim.opengles2;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.Random;

public class EpilepsyActivity extends Activity
{
  // this is the surface where the drawing will actually occur.
  private GLSurfaceView surface;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // We setup the view and enable the use of OpenGL ES 2.0
    // We also set the renderer class, responsible for the drawing logic
    surface = new GLSurfaceView(this);
    surface.setEGLContextClientVersion(2);
    surface.setRenderer(new EpilepsyRenderer());

    // The widget (view) of our gui is the OpenGL surface.
    setContentView(surface);
  }

  @Override
  protected void onResume()
  {
    // Don't forget to RESUME the view when the app resumes
    super.onResume();
    surface.onResume();
  }

  @Override
  protected void onPause()
  {
    // Don't forget to PAUSE the view when the app stops!
    // If you don't do this, the process will still be running,
    // totally screwing the performance and the battery life of the
    // device.
    super.onPause();
    surface.onPause();
  }

  // This is where the drawing logic lives
  private class EpilepsyRenderer implements GLSurfaceView.Renderer
  {
    // Random number generator used to set the background color
    private Random r = new Random();

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
      // This method will be called EVERY TIME our application resumes.
      // When the app pauses, the surfaces and all of the OpenGL resources
      // are freed (textures, etc). Since we don't use anything special in
      // this sample, we don't need to do anything.
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
      // Called when the surface change size or right after it's created.
      // Generally, you will set Viewport and camera options here.
      GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
      // This is the drawing of every app frame. Nothing really special here,
      // We just set the clearColor to random values and clear the screen using
      // that color.
      GLES20.glClearColor(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1.0f);
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
  }
}
