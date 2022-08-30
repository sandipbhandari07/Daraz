package project.clone.daraz;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterProducts extends Filter {

    private AdapterProuctsSeller adapter;
    private ArrayList<ModelProduct> filterlist;

    public FilterProducts(AdapterProuctsSeller adapter, ArrayList<ModelProduct> filterlist) {
        this.adapter = adapter;
        this.filterlist = filterlist;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results=new FilterResults();

        if (charSequence != null && charSequence.length() > 0){


            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelProduct> filtermodels = new ArrayList<>();
            for (int i=0; i<filterlist.size(); i++)
            {
                if (filterlist.get(i).getProducttitle().toUpperCase().contains(charSequence) ||
                filterlist.get(i).getProductcategory().toUpperCase().contains(charSequence) ){
                    filtermodels.add(filterlist.get(i));

            }
            }
            results.count=filtermodels.size();
            results.values=filtermodels;
        }else {
            results.count=filterlist.size();
            results.values=filterlist;

        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        adapter.productlist=(ArrayList<ModelProduct>) filterResults.values;

        adapter.notifyDataSetChanged();
    }
}
