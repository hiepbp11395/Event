package com.fpt.study.yticket.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fpt.study.yticket.Adapter.NavListAdapter;
import com.fpt.study.yticket.R;
import com.fpt.study.yticket.model.NavItem;
import com.fpt.study.yticket.model.Token;
import com.fpt.study.yticket.model.User;
import com.fpt.study.yticket.service.EventService;
import com.fpt.study.yticket.service.UserService;
import com.fpt.study.yticket.util.ServiceGenerator;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavDrawerActivity extends ActionBarActivity {

    DrawerLayout drawerLayout;
    RelativeLayout drawerPane;
    ListView listNav;
    ImageView profile_image;
    TextView profile_username;
    Button btn_profile_login;
    Button btn_profile_signup;

    List<NavItem> navItemList;
    List<Fragment> fragmentList;

    UserService userService;
    boolean isLogin;
    Token token;

    public static final String SHAREDPREFERENCES = "YTicketPrefs";
    public static final String PREF_TOKEN = "UserToken";
    public static final String TAG = "NavFragment";

    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slide_menu_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Gson gson = new Gson();

        SharedPreferences pref = getSharedPreferences(SHAREDPREFERENCES, Context.MODE_PRIVATE);

        String json = pref.getString(PREF_TOKEN, "");

        if (json.equals("")) {
            userService = ServiceGenerator.createService(UserService.class);
            isLogin = false;
        } else {
            token = gson.fromJson(json, Token.class);
            userService = ServiceGenerator.createService(UserService.class, token);
            isLogin = true;
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerPane = (RelativeLayout) findViewById(R.id.drawer_pane);
        profile_image = (ImageView) findViewById(R.id.profile_image);
        profile_username = (TextView) findViewById(R.id.textview_profile_username);
        btn_profile_login = (Button) findViewById(R.id.btn_profile_login);
        btn_profile_signup = (Button) findViewById(R.id.btn_profile_signup);

        btn_profile_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickProfileButtonLogin(v);
            }
        });

        btn_profile_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickProfileBButtonSignUp(v);
            }
        });

        listNav = (ListView) findViewById(R.id.nav_list);

        navItemList = new ArrayList<NavItem>();
        navItemList.add(new NavItem("Home", "Home page", R.drawable.home_icon));
        navItemList.add(new NavItem("User for DEMO", "List of user", R.drawable.user_image));
        navItemList.add(new NavItem("About Us", "", R.drawable.about_us));
        navItemList.add(new NavItem("Logout", "", R.drawable.logout));

        final NavListAdapter navListAdapter =
                new NavListAdapter(getApplicationContext(), R.layout.item_nav_list, navItemList);

        listNav.setAdapter(navListAdapter);
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(new HomeActivity().createFragment());
        fragmentList.add(new UserActivity().createFragment());


        //load HomeFragment as default
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_contain, fragmentList.get(0))
                .commit();

        setTitle(navItemList.get(0).getTitle());
        listNav.setItemChecked(0, true);
        drawerLayout.closeDrawer(drawerPane);


        //set OnClickListener for each items
        listNav.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (navItemList.get(position).getTitle().equalsIgnoreCase("logout")) {
                    Call<Void> call = userService.logout();
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            btn_profile_login.setVisibility(View.VISIBLE);
                            btn_profile_signup.setVisibility(View.VISIBLE);
                            profile_image.setVisibility(View.GONE);
                            profile_username.setVisibility(View.GONE);
                            Intent intent = new Intent(getApplication(), LoginActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {

                        }
                    });
                }
                else if (navItemList.get(position).getTitle().equalsIgnoreCase("about us")){
                    Intent intent = new Intent(getApplication(), AboutUsActivity.class);
                    startActivity(intent);
                }
                else {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_contain, fragmentList.get(position))
                            .commit();

                    setTitle(navItemList.get(position).getTitle());
                    listNav.setItemChecked(position, true);
                    drawerLayout.closeDrawer(drawerPane);
                }
            }
        });


        //create listener for DrawerLayout
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_opened, R.string.drawer_closed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu();
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        //set listener for User Profile Image and Username
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        profile_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        getProfile();

    }


    private void onClickProfileButtonLogin(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void onClickProfileBButtonSignUp(View v) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    private void getProfile() {
        Log.d(TAG, "getProfile started");
        Call<User> call = userService.getCurrentUser();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccess()) {
                    User user = response.body();
                    profile_image.setImageResource(R.drawable.about_us);
                    profile_username.setText(user.getUsername());
                } else {
                    Log.d(TAG, "Get Profile failed");
                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Gson gson = new Gson();

        SharedPreferences pref = getSharedPreferences(SHAREDPREFERENCES, Context.MODE_PRIVATE);

        String json = pref.getString(PREF_TOKEN, "");

        if (json.equals("")) {
            userService = ServiceGenerator.createService(UserService.class);
            isLogin = false;
        } else {
            token = gson.fromJson(json, Token.class);
            userService = ServiceGenerator.createService(UserService.class, token);
            isLogin = true;
        }
    }
}
