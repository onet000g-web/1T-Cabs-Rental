package com.onecabs.rental;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import org.json.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    LinearLayout tollBox; EditText customer, vehicle, contact, from, to, depDate, depTime, retDate, retTime, rent;
    TextView totalToll, grandTotal; int billNo;
    final int BLUE=Color.rgb(12,102,179), DARK=Color.rgb(7,84,154), GREEN=Color.rgb(7,134,78), TEXT=Color.rgb(13,40,86);
    ArrayList<Toll> tolls=new ArrayList<>();
    @Override public void onCreate(Bundle b){super.onCreate(b); billNo=getPreferences(0).getInt("bill_no",1001); build();}
    TextView tv(String s,int sp){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(TEXT);t.setPadding(8,5,8,5);return t;}
    EditText ed(String hint){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(15);e.setSingleLine(true);e.setPadding(14,2,14,2);return e;}
    LinearLayout row(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setPadding(8,3,8,3);return r;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}
    void build(){
        ScrollView sv=new ScrollView(this); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(12,12,12,20);sv.addView(root);setContentView(sv);
        TextView h=tv("🚕  1T Cabs' & Rental",26);h.setTextColor(Color.WHITE);h.setTypeface(null,1);h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(18,18,18,18);h.setBackgroundColor(DARK);root.addView(h,new LinearLayout.LayoutParams(-1,70));
        TextView sub=tv("Create a bill matching your sample format",15);sub.setGravity(Gravity.CENTER);root.addView(sub);
        customer=add(root,"Customer Name","Sahil"); vehicle=add(root,"Vehicle No.","3092"); contact=add(root,"Contact Number","+918006555116"); from=add(root,"Trip From","Faridpur"); to=add(root,"Trip To","Piran KaliyAR");
        LinearLayout d1=row(); depDate=addTo(d1,"Departure Date","04 Oct");depTime=addTo(d1,"Departure Time","11:00 AM");root.addView(d1); LinearLayout d2=row(); retDate=addTo(d2,"Return Date","06 Oct");retTime=addTo(d2,"Return Time","02:00 PM");root.addView(d2);
        rent=add(root,"Car Rent (₹ / 24 Hours)","2000");
        TextView th=tv("TOLL DETAILS",19);th.setTypeface(null,1);th.setTextColor(Color.WHITE);th.setBackgroundColor(BLUE);root.addView(th,new LinearLayout.LayoutParams(-1,52));
        tollBox=new LinearLayout(this);tollBox.setOrientation(LinearLayout.VERTICAL);root.addView(tollBox); addToll();
        Button add=btn("＋ Add Toll Plaza"); add.setOnClickListener(v->addToll());root.addView(add);
        totalToll=tv("Total Toll Charges: ₹0",18);totalToll.setTypeface(null,1);root.addView(totalToll); grandTotal=tv("Grand Total: ₹2000",21);grandTotal.setTypeface(null,1);grandTotal.setTextColor(GREEN);root.addView(grandTotal);
        Button gen=btn("GENERATE BILL PREVIEW");gen.setTextColor(Color.WHITE);gen.setBackgroundColor(GREEN);gen.setOnClickListener(v->generate());root.addView(gen,new LinearLayout.LayoutParams(-1,58));
        Button history=btn("Bill History");history.setOnClickListener(v->showHistory());root.addView(history);
    }
    EditText add(LinearLayout p,String hint,String val){TextView l=tv(hint,12);p.addView(l);EditText e=ed(hint);e.setText(val);p.addView(e,new LinearLayout.LayoutParams(-1,52));return e;}
    EditText addTo(LinearLayout p,String hint,String val){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);TextView l=tv(hint,11);c.addView(l);EditText e=ed(hint);e.setText(val);c.addView(e,new LinearLayout.LayoutParams(-1,48));p.addView(c,new LinearLayout.LayoutParams(0,90,1));return e;}
    void addToll(){
        Toll t=new Toll(); tolls.add(t); LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(8,8,8,8);box.setBackgroundColor(Color.rgb(238,247,255));
        EditText name=ed("Toll Name"); box.addView(name); Spinner type=new Spinner(this);String[] types={"Single + Single (>24 hrs)","Return Trip (<24 hrs)","Local Pass"};type.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,types));box.addView(type); type.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p, View v,int pos,long id){calc();} public void onNothingSelected(android.widget.AdapterView<?> p){}});
        LinearLayout r1=row();EditText single=ed("Single ₹");EditText ret=ed("Return ₹");r1.addView(single,new LinearLayout.LayoutParams(0,55,1));r1.addView(ret,new LinearLayout.LayoutParams(0,55,1));box.addView(r1);
        LinearLayout r2=row();EditText go=ed("Jaate Time");EditText back=ed("Aate Time");r2.addView(go,new LinearLayout.LayoutParams(0,55,1));r2.addView(back,new LinearLayout.LayoutParams(0,55,1));box.addView(r2);
        Button del=btn("Remove");del.setOnClickListener(v->{tolls.remove(t);tollBox.removeView(box);calc();});box.addView(del); tollBox.addView(box); t.bind(name,type,single,ret,go,back); calc();
    }
    void calc(){double total=0;for(Toll t:tolls){try{double s=Double.parseDouble(t.single.getText().toString());double r=Double.parseDouble(t.ret.getText().toString());int ty=t.type.getSelectedItemPosition();if(ty==0)total+=s*2;else if(ty==1)total+=r;}catch(Exception ignored){}}totalToll.setText("Total Toll Charges: ₹"+money(total));double rr=val(rent);grandTotal.setText("Grand Total: ₹"+money(rr+total));}
    double val(EditText e){try{return Double.parseDouble(e.getText().toString());}catch(Exception x){return 0;}}
    String money(double n){return String.format(Locale.US,"%.0f",n);}
    void generate(){save();Intent i=new Intent(this,BillPreviewActivity.class);i.putExtra("json",buildJson().toString());startActivity(i);}
    JSONObject buildJson(){JSONObject o=new JSONObject();try{o.put("bill",billNo);o.put("customer",customer.getText().toString());o.put("vehicle",vehicle.getText().toString());o.put("contact",contact.getText().toString());o.put("from",from.getText().toString());o.put("to",to.getText().toString());o.put("depDate",depDate.getText().toString());o.put("depTime",depTime.getText().toString());o.put("retDate",retDate.getText().toString());o.put("retTime",retTime.getText().toString());o.put("rent",val(rent));JSONArray a=new JSONArray();double total=0;for(Toll t:tolls){JSONObject x=new JSONObject();x.put("name",t.name.getText().toString());x.put("type",t.type.getSelectedItemPosition());x.put("single",val(t.single));x.put("return",val(t.ret));x.put("go",t.go.getText().toString());x.put("back",t.back.getText().toString());a.put(x);if(t.type.getSelectedItemPosition()==0)total+=val(t.single)*2;else if(t.type.getSelectedItemPosition()==1)total+=val(t.ret);}o.put("tolls",a);o.put("tollTotal",total);o.put("grand",total+val(rent));}catch(Exception ignored){}return o;}
    void save(){getPreferences(0).edit().putInt("bill_no",billNo+1).apply();billNo++;String s=getPreferences(0).getString("history","[]");try{JSONArray a=new JSONArray(s);a.put(buildJson());getPreferences(0).edit().putString("history",a.toString()).apply();}catch(Exception ignored){}}
    void showHistory(){try{JSONArray a=new JSONArray(getPreferences(0).getString("history","[]"));StringBuilder s=new StringBuilder();for(int i=a.length()-1;i>=0;i--){JSONObject o=a.getJSONObject(i);s.append("Bill #").append(o.optInt("bill")).append(" — ").append(o.optString("customer")).append(" — ₹").append(o.optString("grand")).append("\n");}new AlertDialog.Builder(this).setTitle("Saved Bills").setMessage(s.length()==0?"No bills saved yet.":s.toString()).setPositiveButton("OK",null).show();}catch(Exception ignored){}}
    static class Toll {EditText name,single,ret,go,back;Spinner type;void bind(EditText n,Spinner t,EditText s,EditText r,EditText g,EditText b){name=n;type=t;single=s;ret=r;go=g;back=b;}}
}

class BillPreviewActivity extends Activity {
    JSONObject data; BillView view;
    @Override public void onCreate(Bundle b){super.onCreate(b);try{data=new JSONObject(getIntent().getStringExtra("json"));}catch(Exception e){data=new JSONObject();}LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);Button share=new Button(this);share.setText("Share PDF / Print");share.setOnClickListener(v->makePdf());l.addView(share);view=new BillView(this,data);l.addView(view,new LinearLayout.LayoutParams(-1,0,1));setContentView(l);}
    void makePdf(){try{PdfDocument doc=new PdfDocument();PdfDocument.PageInfo pi=new PdfDocument.PageInfo.Builder(1240,1754,1).create();PdfDocument.Page p=doc.startPage(pi);view.drawBill(p.getCanvas(),1240,1754);doc.finishPage(p);File f=new File(getExternalFilesDir("Documents"),"Bill_"+data.optInt("bill")+".pdf");FileOutputStream out=new FileOutputStream(f);doc.writeTo(out);out.close();doc.close();Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,FileProvider.getUriForFile(this,getPackageName()+".fileprovider",f));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share Bill"));}catch(Exception e){new AlertDialog.Builder(this).setMessage(e.toString()).setPositiveButton("OK",null).show();}}
}

class BillView extends View {
    Paint p=new Paint(3); JSONObject d; int blue=Color.rgb(12,102,179),dark=Color.rgb(7,84,154),green=Color.rgb(7,134,78),text=Color.rgb(13,40,86),line=Color.rgb(139,191,230);
    BillView(Context c,JSONObject o){super(c);d=o;p.setTypeface(Typeface.create("sans",Typeface.NORMAL));setBackgroundColor(Color.WHITE);}
    void rect(Canvas c,float l,float t,float r,float b,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawRect(l,t,r,b,p);}void stroke(Canvas c,float l,float t,float r,float b){p.setColor(line);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawRect(l,t,r,b,p);p.setStyle(Paint.Style.FILL);}
    void txt(Canvas c,String s,float x,float y,float size,int col,boolean bold){p.setTextSize(size);p.setColor(col);p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,p);}
    void center(Canvas c,String s,float x,float y,float size,int col,boolean bold){p.setTextSize(size);float w=p.measureText(s);txt(c,s,x-w/2,y,size,col,bold);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);drawBill(c,getWidth(),getHeight());}
    void drawBill(Canvas c,int W,int H){float sx=W/1240f, sy=H/1754f;c.save();c.scale(sx,sy);
        rect(c,12,12,1228,135,dark);txt(c,"🚕",35,82,55,Color.WHITE,true);txt(c,"1T Cabs’ & Rental",150,72,49,Color.WHITE,true);txt(c,"Vehicle No. "+d.optString("vehicle")+"   |   Contact: "+d.optString("contact"),152,111,22,Color.WHITE,true);txt(c,"Trip:",870,52,20,Color.WHITE,true);txt(c,d.optString("from")+"  ⇄  "+d.optString("to"),870,90,27,Color.WHITE,true);
        rect(c,12,150,1228,245,Color.rgb(234,245,255));stroke(c,12,150,1228,245);txt(c,"Customer Name",100,178,17,text,true);txt(c,d.optString("customer"),100,216,30,text,true);txt(c,"Car Rent",470,178,17,text,true);txt(c,"₹"+money(d.optDouble("rent"))+" / 24 Hours",470,216,27,text,true);txt(c,"Departure ("+d.optString("from")+")",815,178,17,text,true);txt(c,d.optString("depDate")+", "+d.optString("depTime"),815,216,24,text,true);txt(c,"Return ("+d.optString("from")+")",1060,178,17,text,true);txt(c,d.optString("retDate")+", "+d.optString("retTime"),1060,216,24,text,true);
        float y=255;float[] xs={12,110,410,735,1015,1228};rect(c,12,y,1228,y+75,blue);String[] heads={"S.No.","Toll Name","Toll Timing","Journey","Toll Price"};for(int i=0;i<5;i++)center(c,heads[i],(xs[i]+xs[i+1])/2,y+32,18,Color.WHITE,true);center(c,"(Jaate Time → Aate Time)",572,y+58,12,Color.WHITE,true);center(c,"(As per your rules)",875,y+58,12,Color.WHITE,true);center(c,"(As per your rules)",1120,y+58,12,Color.WHITE,true);
        try{JSONArray a=d.getJSONArray("tolls");float rh=92;for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);float top=y+75+i*rh;rect(c,12,top,1228,top+rh,i%2==0?Color.rgb(234,248,255):Color.rgb(248,252,255));stroke(c,12,top,1228,top+rh);for(float x:xs)c.drawLine(x,top,x,top+rh,p);center(c,""+(i+1),61,top+48,22,text,true);txt(c,o.optString("name"),128,top+35,20,text,true);txt(c,o.optString("go")+" →",465,top+30,16,text,false);txt(c,o.optString("back"),465,top+58,16,text,false);int type=o.optInt("type");String jt=type==0?"Single + Single":type==1?"Return Trip":"Local Pass";txt(c,jt,765,top+32,18,text,true);txt(c,type==0?"(2 trips, > 24 hrs)":type==1?"(2 trips, < 24 hrs)":"(Local Pass)",765,top+58,13,text,false);double amount=type==0?o.optDouble("single")*2:type==1?o.optDouble("return"):0;txt(c,"₹"+money(amount),1080,top+47,20,text,true);}}
        float tableBottom=y+75+a.length()*92;float syy=tableBottom+18;rect(c,12,syy,735,syy+230,Color.rgb(247,252,255));stroke(c,12,syy,735,syy+230);rect(c,12,syy,735,syy+48,dark);txt(c,"Toll Price Reference (Per Journey)",30,syy+33,20,Color.WHITE,true);String[] n={"Bhaguwala Toll Plaza","Niyamatpur Ekrotiya Toll Plaza","Thiriya Khetal Toll Plaza","Koyla Toll Plaza","Faridpur Toll Plaza"};int[] s={150,165,165,55,0},r={220,245,245,85,0};for(int i=0;i<5;i++){float yy=syy+78+i*34;txt(c,n[i],28,yy,13,text,false);txt(c,"₹"+s[i],390,yy,13,text,false);txt(c,"₹"+r[i],560,yy,13,text,false);}rect(c,755,syy,1228,syy+230,Color.rgb(239,252,245));stroke(c,755,syy,1228,syy+230);rect(c,755,syy,1228,syy+48,green);txt(c,"🚕  Car Rent & Toll Bill",785,syy+33,21,Color.WHITE,true);txt(c,"Car Rent (24 Hours)        :   ₹"+money(d.optDouble("rent")),790,syy+100,17,text,true);txt(c,"Total Toll Charges        :   ₹"+money(d.optDouble("tollTotal")),790,syy+140,17,text,true);rect(c,775,syy+165,1210,syy+216,green);txt(c,"Grand Total  =  ₹"+money(d.optDouble("grand")),830,syy+199,26,Color.WHITE,true);
        float fy=syy+250;rect(c,12,fy,735,fy+100,Color.rgb(234,245,255));stroke(c,12,fy,735,fy+100);txt(c,"Trip:  "+d.optString("from")+"  ⇄  "+d.optString("to"),145,fy+28,15,text,true);txt(c,"Customer:  "+d.optString("customer"),145,fy+52,15,text,true);txt(c,"Vehicle No.:  "+d.optString("vehicle"),145,fy+76,15,text,true);txt(c,"Contact:  "+d.optString("contact"),145,fy+100,15,text,true);rect(c,755,fy,1228,fy+100,Color.rgb(234,245,255));stroke(c,755,fy,1228,fy+100);txt(c,"Departure: "+d.optString("depDate")+" "+d.optString("depTime"),790,fy+42,17,text,true);txt(c,"Return: "+d.optString("retDate")+" "+d.optString("retTime"),790,fy+76,17,text,true);
        }catch(Exception ignored){}c.restore();}
    String money(double n){return String.format(Locale.US,"%.0f",n);}
}
