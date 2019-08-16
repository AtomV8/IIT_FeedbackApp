package ch.fhnw.ip6_feedbackapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ch.fhnw.ip6_feedbackapp.AppDetectionService.SOURCE_FEEDBACK_APP;
import static ch.fhnw.ip6_feedbackapp.AppDetectionService.TYPE_DEFAULT;

public class MyAppsFragment extends Fragment {

    ListView listView;
    ProgressBar progressBar;
    SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_apps, container, false);
        listView = view.findViewById(R.id.appsList);
        refreshLayout = view.findViewById(R.id.swiperefreshAppsList);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                InstalledApps installedApps = new InstalledApps();
                installedApps.start();
            }
        });


        progressBar = view.findViewById(R.id.appsListProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        InstalledApps installedApps = new InstalledApps();
        installedApps.start();
        return view;
    }

    public void onAppsListReceived(ArrayList<AppDetails> appsList) {
        refreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        AppsListAdapter appsListAdapter = new AppsListAdapter(getActivity(), appsList);
        listView.setAdapter(appsListAdapter);
    }

    // List adapter for list of installed apps
    class AppsListAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<AppDetails> installedApps;

        AppsListAdapter(Context c, ArrayList<AppDetails> apps) {
            super(c, R.layout.apps_list_entry);
            this.installedApps = apps;
            this.context = c;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the row elements for the view
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View row = layoutInflater.inflate(R.layout.apps_list_entry, parent, false);
            ImageView appImage = row.findViewById(R.id.appIcon);
            TextView appTitle = row.findViewById(R.id.appPageTitle);

            // Fill in the rows
            appImage.setImageDrawable(installedApps.get(position).getAppIcon());
            appTitle.setText(installedApps.get(position).getAppName());
            final String packageName = installedApps.get(position).getPackageName();

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Open current app's page in feedback app if notification body clicked
                    Intent appFeedbackIntent = new Intent(getActivity(), FeedbackActivity.class);
                    appFeedbackIntent.putExtra("App", packageName);
                    appFeedbackIntent.putExtra("Type", TYPE_DEFAULT);
                    appFeedbackIntent.putExtra("Source", SOURCE_FEEDBACK_APP);
                    startActivity(appFeedbackIntent);
                }
            });
            return row;
        }

        @Override
        public int getCount() {
            return installedApps.size();
        }
    }

    private class InstalledApps extends Thread {

        private void getInstalledApps() {
            final ArrayList<AppDetails> res = new ArrayList<>();
            List<PackageInfo> packs = getContext().getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packs.size(); i++) {
                PackageInfo p = packs.get(i);
                if ((isSystemPackage(p) == false)) {
                    String packageName = p.packageName;
                    String appName = p.applicationInfo.loadLabel(getContext().getPackageManager()).toString();
                    Drawable icon = p.applicationInfo.loadIcon(getContext().getPackageManager());
                    String packageVersion = p.versionName;
                    res.add(new AppDetails(packageName, appName, icon, packageVersion));
                }
            }
            Collections.sort(res);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onAppsListReceived(res);
                }
            });

        }

        @Override
        public void run() {
            getInstalledApps();
        }

        // Helper method to exclude system apps
        private boolean isSystemPackage(PackageInfo pkgInfo) {
            return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        }
    }

}
