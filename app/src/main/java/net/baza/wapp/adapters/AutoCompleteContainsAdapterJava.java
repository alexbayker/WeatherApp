package net.baza.wapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ProgressBar;
//import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoCompleteContainsAdapterJava<T> extends ArrayAdapter<String>
{
    public static final int WAITTIME = 1000;

    public CopyOnWriteArrayList<T> objects  = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<String> suggestions = new CopyOnWriteArrayList<>();
    public Date lastdate;
    public AutoCompleteTextView textview;
    public ProgressBar loadingbar = null;
    public String lasttext;
    public int resourceid;

    public FilterObjects<T> filtercallback;

    public interface FilterObjects<T>
    {
        List<T> get(String searchrequest);
    }

    public AutoCompleteContainsAdapterJava(Context context, int resource, AutoCompleteTextView textview, FilterObjects<T> filtercallback)
    {
        super(context, resource, new CopyOnWriteArrayList<>());
        this.resourceid = resource;
        this.textview = textview;
        this.lastdate = new Date();
        this.lasttext = "";
        this.filtercallback = filtercallback;
    }

    public AutoCompleteContainsAdapterJava(Context context, int resource, AutoCompleteTextView textview, ProgressBar loadingbar, FilterObjects<T> filtercallback)
    {
        this(context, resource, textview, filtercallback);
        this.loadingbar = loadingbar;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        /*if (v instanceof TextView)
        {
            TextView textview = (TextView) v;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Context context = parent.getContext();
            if (context instanceof Activity)
            {
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                String text = getDensityName(context);
                if (text.equals("ldpi") || text.equals("hdpi") || text.equals("mdpi"))
                    textview.setText(textview.getText().toString().replaceAll("[,] ", ", \n"));
                else
                    textview.setText(textview.getText().toString().replaceAll("[,] ", ", \n").replaceFirst("[,] \n", ", "));
            }
        }*/
        return v;
    }

    public T getObjectByString(String string)
    {
        string = string.trim();
        for (T object : objects)
            if (object.toString().equals(string))
                return object;
        return null;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        /*public String convertResultToString(Object resultValue) {
            String str = ((ProductDataModel) (resultValue)).getProductName();
            return str;
        }*/

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            if (constraint != null)
            {
                lastdate = new Date();
                lasttext = constraint.toString();
                AtomicInteger needend = new AtomicInteger(0);
                new Thread(() -> {
                    try {
                        int sleeptime = 0;
                        while (lasttext.equals(textview.getText().toString()))
                        {
                            Thread.sleep(10);
                            sleeptime += 10;
                            if (sleeptime > WAITTIME/2)
                                break;
                            if (sleeptime > 200)
                                loadingBarVisibility(View.VISIBLE);
                        }
                        if (lasttext.equals(textview.getText().toString()) && sleeptime > WAITTIME/2)
                        {
                            loadingBarVisibility(View.VISIBLE);
                            objects.clear();
                            suggestions.clear();

                            for (T object : filtercallback.get(lasttext))
                            {
                                if (suggestions.size() > 25)
                                    break;
                                suggestions.add(object.toString());
                                objects.add(object);
                            }
                            needend.set(1);
                        }
                        else
                            needend.set(-1);
                    } catch (Exception e)
                    {
                        Log.e("ContainsAdapterError", "Error updating info: ", e);
                    }
                }).start();

                while (needend.get() == 0)
                {
                    try
                    {
                        Thread.sleep(10);
                    } catch (Exception ignored) {}
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        public void loadingBarVisibility(int visibility)
        {

            if (loadingbar != null)
            {
                Context context = loadingbar.getContext();
                if (context instanceof Activity)
                {
                    ((Activity) context).runOnUiThread(() -> {
                        try {
                            loadingbar.setVisibility(visibility);
                        }
                        catch (Exception ignored) { }
                    });
                }
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            @SuppressWarnings("unchecked")
            CopyOnWriteArrayList<String> filteredList = (CopyOnWriteArrayList<String>) results.values;
            if (filteredList != null && results.count > 0) {
                clear();
                for (String s : filteredList)
                    add(s);
                lastdate = new Date();
                notifyDataSetChanged();
            }
            loadingBarVisibility(View.GONE);
        }
    };

    //масштаб экрана
    public static String getDensityName(Context context)
    {
        float density = context.getResources().getDisplayMetrics().density;
        if (density >= 4.0)
            return "xxxhdpi";
        if (density >= 3.0)
            return "xxhdpi";
        if (density >= 2.0)
            return "xhdpi";
        if (density >= 1.5)
            return "hdpi";
        if (density >= 1.0)
            return "mdpi";
        return "ldpi";
    }
}