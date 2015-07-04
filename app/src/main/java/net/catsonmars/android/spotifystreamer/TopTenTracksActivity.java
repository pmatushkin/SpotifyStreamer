package net.catsonmars.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class TopTenTracksActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);
        setSubTitle();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_ten_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                /*
                this is to make sure the screen the Up button returns us to is populated with the search results
                see http://stackoverflow.com/a/20306670 for details
                 */
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setSubTitle() {
        String extra = Intent.EXTRA_TITLE;

        String subTitle = "";
        Intent intent = getIntent();
        if ((intent != null) && intent.hasExtra(extra)) {
            subTitle = intent.getStringExtra(extra);

            if (!subTitle.isEmpty()) {
                ActionBar ab = getSupportActionBar();
                ab.setSubtitle(subTitle);
            }
        }
    }
}