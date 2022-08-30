package project.clone.daraz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AdapterProuctsSeller extends RecyclerView.Adapter<AdapterProuctsSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productlist,filterlist;

    private FilterProducts filter;
    public AdapterProuctsSeller(Context context, ArrayList<ModelProduct> productlist) {
        this.context = context;
        this.productlist = productlist;
        this.filterlist = productlist;
    }



    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_produts_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        ModelProduct modelProduct = productlist.get(position);
        String sid=modelProduct.getProductid();
        String suid=modelProduct.getUid();
        String sdiscpuntavailable=modelProduct.getDiscountavailable();
        String sdiscountnote=modelProduct.getDiscountnotes();
        String sdiscountprice= modelProduct.getDiscountprice();
        String sproductcategory = modelProduct.getProductcategory();
        String sproductdesc=modelProduct.getProductdesc();
        String sicon=modelProduct.getProducticon();
        String squantity=modelProduct.getProductquantity();
        String stitle=modelProduct.getProducttitle();
        String stimestamp=modelProduct.getTimestamp();
        String orginalprice = modelProduct.getOrginalprice();

        holder.titletv.setText(stitle);
        holder.quantitytv.setText(squantity);
        holder.discountnotetv.setText(sdiscountnote);
        holder.discountedpricetv.setText("Rs"+sdiscountprice);
        holder.orginalpricetv.setText("Rs"+orginalprice);
        if (sdiscpuntavailable.equals("true"))
        {
            //products is on discount
            holder.discountedpricetv.setVisibility(View.VISIBLE);
            holder.discountnotetv.setVisibility(View.VISIBLE);
            holder.orginalpricetv.setPaintFlags(holder.orginalpricetv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        else
        {
            holder.discountedpricetv.setVisibility(View.GONE);
            holder.discountnotetv.setVisibility(View.GONE);

        }
        try
        {
            Picasso.get().load(sicon).placeholder(R.drawable.nav_cart).into(holder.productsiconiv);
        }
        catch (Exception e){
            holder.productsiconiv.setImageResource(R.drawable.shop);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //handle item clicks, show item details
                detailsbottomsheet(modelProduct);
            }
        });
    }

    private void detailsbottomsheet(ModelProduct modelProduct) {

        BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(context);
        View view=LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        bottomSheetDialog.setContentView(view);

        bottomSheetDialog.show();

        ImageButton abackbtn=view.findViewById(R.id.ds_back);
        ImageButton adeletbtn=view.findViewById(R.id.ds_delete);
        ImageButton aeditbtn=view.findViewById(R.id.ds_edit);
        ImageView aproductIcon=view.findViewById(R.id.ds_productIconTv);
        TextView adiscountnote=view.findViewById(R.id.ds_discountNoteTv);
        TextView atitletv=view.findViewById(R.id.ds_title);
        TextView adescription=view.findViewById(R.id.ds_desc);
        TextView acategory=view.findViewById(R.id.ds_category);
        TextView aquantity=view.findViewById(R.id.ds_quantity);
        TextView adiscountprice=view.findViewById(R.id.ds_discountPriceTv);
        TextView aorginal=view.findViewById(R.id.ds_orginalprice);


        String sid=modelProduct.getProductid();
        String suid=modelProduct.getUid();
        String sdiscpuntavailable=modelProduct.getDiscountavailable();
        String sdiscountnote=modelProduct.getDiscountnotes();
        String sdiscountprice= modelProduct.getDiscountprice();
        String sproductcategory = modelProduct.getProductcategory();
        String sproductdesc=modelProduct.getProductdesc();
        String sicon=modelProduct.getProducticon();
        String squantity=modelProduct.getProductquantity();
        String stitle=modelProduct.getProducttitle();
        String stimestamp=modelProduct.getTimestamp();
        String orginalprice = modelProduct.getOrginalprice();

        //set data
        atitletv.setText(stitle);
        adescription.setText(sproductdesc);
        aquantity.setText(squantity);
        adiscountnote.setText(sdiscountnote);
        adiscountprice.setText(sdiscountprice);
        aorginal.setText(orginalprice);
        acategory.setText(sproductcategory);
        if (sdiscpuntavailable.equals("true"))
        {
            adiscountprice.setVisibility(View.VISIBLE);
            adiscountnote.setVisibility(View.VISIBLE);
            aorginal.setPaintFlags(aorginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else
        {
            adiscountprice.setVisibility(View.GONE);
            adiscountnote.setVisibility(View.GONE);
        }
        try
        {
            Picasso.get().load(sicon).placeholder(R.drawable.nav_cart).into(aproductIcon);
        }
        catch (Exception e){
            aproductIcon.setImageResource(R.drawable.shop);
        }

        bottomSheetDialog.show();

        aeditbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,EditProducts.class);
                intent.putExtra("productid",sid);
                context.startActivity(intent);

            }
        });
        adeletbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Do you want to delete product"+stitle+" ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteproduct(sid);
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });
        abackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void deleteproduct(String sid) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(sid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productlist.size();
    }

    @Override
    public Filter getFilter() {
       if (filter==null){
           filter = new FilterProducts(this,filterlist);
       }
       return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{

        private ImageView productsiconiv;
        private TextView discountnotetv,titletv,quantitytv,discountedpricetv,orginalpricetv;
        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productsiconiv=itemView.findViewById(R.id.sproductIconiv);
            discountnotetv=itemView.findViewById(R.id.sdiscountnotetv);
            titletv=itemView.findViewById(R.id.stitletv);
            quantitytv=itemView.findViewById(R.id.squantitytv);
            discountedpricetv=itemView.findViewById(R.id.sdiscountpricetv);
            orginalpricetv=itemView.findViewById(R.id.sorginalpricetv);
        }
    }
}
