package ca.staugustinechs.staugustineapp.Activities;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.IconSelectFragment;
import ca.staugustinechs.staugustineapp.R;

public class IconSelect extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iconselect);
        this.getSupportActionBar().setTitle("Profile Picture Selection");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        IconSelectFragment fragment = new IconSelectFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("SIGNUP", false);
        fragment.setArguments(bundle);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.icon_fragment_container, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
