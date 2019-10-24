package ca.staugustinechs.staugustineapp.Activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.IconSelectFragment;
import ca.staugustinechs.staugustineapp.R;

public class IconSelect extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iconselect);
        //SET TITLE, ALLOW BACK ARROW IN TOP LEFT CORNER TO FINISH ACTIVITY, AND SET COLOR
        this.getSupportActionBar().setTitle("Profile Picture Selection");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        //SHOW ICON SELECT FRAGMENT
        super.onPostCreate(savedInstanceState);
        IconSelectFragment fragment = new IconSelectFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("SIGNUP", false);
        fragment.setArguments(bundle);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.icon_fragment_container, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //FINISH ACTIVITY WHEN BACK BUTTON IN TOP LEFT CORNER IS PRESSED
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
