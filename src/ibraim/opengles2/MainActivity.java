package ibraim.opengles2;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity
{
  private final String tests[] =
    {"Epilepsy", "Triangle2d", "TriangleColor", "Texture"};

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tests));
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id)
  {
    super.onListItemClick(l, v, position, id);
    String name = tests[position];

    try
    {
      Class c = Class.forName("ibraim.opengles2." + name + "Activity");
      Intent i = new Intent(this, c);
      startActivity(i);
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
  }
}
