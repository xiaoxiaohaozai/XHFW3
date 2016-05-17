package com.zst.xposed.halo.floatingwindow3.floatdot;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.util.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.widget.TableRow.*;
import com.zst.xposed.halo.floatingwindow3.*;
import java.util.*;
import de.robv.android.xposed.*;
import android.content.pm.*;
import android.content.pm.PackageManager.*;
import android.app.*;

public class FloatLauncher
{
	Context mContext;
	PackageManager mPackageManager;
	int mScreenWidth;
	int mScreenHeight;
	int MINIMAL_WIDTH;
	int MINIMAL_HEIGHT;
	ArrayList<PackageItem> itemsList = new ArrayList<PackageItem>();
	ArrayList<String> itemsIndex = new ArrayList<String>();
	
	public FloatLauncher(Context sContext){
		mContext = sContext;
		mPackageManager = mContext.getPackageManager();
		regBroadcastReceiver();
		//fillMenu(itemsList);
	}
	
	public void showMenu(View anchor, WindowManager.LayoutParams paramsF, int offset){
		refreshScreenSize();
		refreshMinimalSize();
		ListView lv = new ListView(mContext);
//		String[] values = new String[] { "Android List View", 
//			"Adapter implementation",
//			"Simple List View In Android",
//			"Create List View Android", 
//			"Android Example", 
//			"List View Source Code", 
//			"List View Array Adapter", 
//			"Android Example List View" 
//		};
//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
//			android.R.layout.simple_list_item_1, android.R.id.text1, values);
		PopupWindow popupWin = new PopupWindow();
		LauncherListAdapter adapter = new LauncherListAdapter(mContext, itemsList, popupWin);
		lv.setAdapter(adapter);
		int width = lv.getWidth();
		int height = lv.getHeight();
		boolean putLeft = false;
		
		popupWin.setContentView(lv);
		popupWin.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWin.setHeight(MINIMAL_HEIGHT);
		width = View.MeasureSpec.getSize(popupWin.getWidth());
		height = View.MeasureSpec.getSize(popupWin.getHeight());
		if(width>mScreenWidth-paramsF.x-offset || width == 0)
			width = mScreenWidth-paramsF.x-offset;
		if(height > mScreenHeight/3 || height == 0)
			height = mScreenHeight/3;
		if(width<MINIMAL_WIDTH){
			width=MINIMAL_WIDTH;
			putLeft=true;
		}
//		if(height<MINIMAL_HEIGHT)
//			height=MINIMAL_HEIGHT;
		popupWin.setWidth(MINIMAL_WIDTH);
		popupWin.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWin.setOutsideTouchable(true);
		popupWin.setClippingEnabled(true);
		ColorDrawable cd = new ColorDrawable(Color.parseColor("#AA333333"));
		popupWin.setBackgroundDrawable(cd);
		popupWin.setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
		
		int x = putLeft? paramsF.x-width: paramsF.x+offset;
		int y = paramsF.y/*+offset-Util.getStatusBarHeight(mContext)*/-mScreenHeight/2; //-height/2;
		popupWin.showAtLocation(anchor, Gravity.CENTER_VERTICAL | Gravity.LEFT, x, y);
	}
	
	private void fillMenu(ArrayList<PackageItem> packages){
		//TODO add pinned apps
		addItem("test1",0,0);
		addItem("be ge er zer ver ent emp mis",0,0);
		addItem("com.zst.xposed.halo.floatingwindow3.floatdot",0,0);
	}
	
	private void refreshScreenSize(){
		final WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		final DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);

		mScreenHeight = metrics.heightPixels;
		mScreenWidth = metrics.widthPixels;
	}
	
	private void refreshMinimalSize(){
		MINIMAL_WIDTH=mScreenWidth/4;
		MINIMAL_HEIGHT=0;
		int minimorum_width = Util.realDp(150, mContext);
		if(MINIMAL_WIDTH<minimorum_width)
			MINIMAL_WIDTH=minimorum_width;
	}
	
	private void addItem(String pkgName, int taskId, int sGravity){
		if(itemsIndex.contains(pkgName))
			return;
		itemsList.add(new PackageItem(mContext.getPackageManager(), pkgName, taskId, sGravity));
		itemsIndex.add(pkgName);
	}
	
	private void removeItem(String pkgName){
		if(!itemsIndex.contains(pkgName))
			return;
		itemsList.remove(itemsIndex.indexOf(pkgName));
	}
	
	private void updateItem(String pkgName, int mTaskId){
		if(!itemsIndex.contains(pkgName))
			return;
		PackageItem pi = itemsList.get(itemsIndex.indexOf(pkgName));
		pi.taskId=mTaskId;
		
	}
	
	final BroadcastReceiver br = new BroadcastReceiver(){

		@Override
		public void onReceive(Context sContext, Intent sIntent)
		{
			if(sIntent.getAction().equals(Common.ORIGINAL_PACKAGE_NAME + ".APP_REMOVED")){
				boolean mCompletely = sIntent.getBooleanExtra("removeCompletely", false);
				if(mCompletely)
					removeItem(sIntent.getStringExtra("packageName"));
				else
					updateItem(sIntent.getStringExtra("packageName"), 0);
			}
			String pkgName = sIntent.getStringExtra("packageName");
			Log.d("Xposed", "FloatingLauncher broadcast package " + (pkgName==null?"null":pkgName));
			if(pkgName==null) return;
			
			int sGravity = sIntent.getIntExtra("float-gravity", 0);
			int taskId = sIntent.getIntExtra("float-taskid", 0);
			if(taskId==0)
				return;
			addItem(pkgName, taskId, sGravity);
		}
	};
	
	private void regBroadcastReceiver(){
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Common.ORIGINAL_PACKAGE_NAME + ".APP_LAUNCHED");
		mIntentFilter.addAction(Common.ORIGINAL_PACKAGE_NAME + ".APP_REMOVED");
		mContext.getApplicationContext().registerReceiver(br, mIntentFilter);
	}
	
//	private void drawFocusFrame(){
//		if(!mAeroFocusWindow) return;
//		//hide previous outlines
//		hideFocusFrame(mContext.getApplicationContext());
//		if(!mWindowHolder.isSnapped) return;
//		//send new params
//		Intent mIntent = new Intent(Common.SHOW_OUTLINE);
//		int[] array = {mWindowHolder.x, mWindowHolder.y, mWindowHolder.height, mWindowHolder.width};
//		mIntent.putExtra(Common.INTENT_APP_PARAMS, array);
//		mIntent.putExtra(Common.INTENT_APP_FOCUS, true);
//		mWindowHolder.mActivity.getApplicationContext().sendBroadcast(mIntent);
//		showFocusOutline = true;
//	}
//	
//	private static void hideFocusFrame(Context mContext){
//		mContext.sendBroadcast(new Intent(Common.SHOW_OUTLINE));
//		showFocusOutline = false;
//	}
}

class PackageItem implements Comparable<PackageItem>{
	public Drawable packageIcon;
	public String packageName;
	public CharSequence title;
	public int snapGravity;
	public int taskId;
	
	public PackageItem(PackageManager mPackageManager, String mPackageName, int mTaskId, int sGravity){
		//ApplicationInfo mAppInfo = mPackageManager.getApp
		Drawable icon;
		try{
			icon = mPackageManager.getApplicationIcon(mPackageName);
		} catch (Throwable t){
			icon = new ColorDrawable(Color.BLACK);
		}
		packageIcon = icon;
		try
		{
			title = mPackageManager.getApplicationInfo(mPackageName, 0).loadLabel(mPackageManager);
		} catch (Throwable e)
		{
			title = mPackageName;
		}
		taskId = mTaskId;
		snapGravity = sGravity;
		packageName = mPackageName;
		// (packageName);
		//= mAppInfo.loadIcon(
	}
	
	public PackageItem(String mPackageName, String mTitle, int mTaskId, Drawable icon, int sGravity){
		packageName = mPackageName;
		title = mTitle;
		taskId = mTaskId;
		packageIcon = icon;
		snapGravity = sGravity;
	}
	public PackageItem(String mPackageName){
		packageName = mPackageName;
		packageIcon = new ColorDrawable(Color.BLACK);
	}
	
	@Override
	public int compareTo(PackageItem another) {
		return this.packageName.toString().compareTo(another.packageName.toString());
	}
}

class LauncherListAdapter extends ArrayAdapter<PackageItem>{
	Context mContext;
	PopupWindow popupWin;
	public LauncherListAdapter(Context sContext, ArrayList<PackageItem> itemsList, PopupWindow mPopupWin){
		super(sContext, 0, itemsList);
		mContext=sContext;
		popupWin=mPopupWin;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		final PackageItem item = getItem(position);    
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.floatdot_launcher_menuitem, parent, false);
		}
		// Lookup view for data population
		ImageView mIcon = (ImageView) convertView.findViewById(android.R.id.icon);
		TextView mTitle = (TextView) convertView.findViewById(android.R.id.text1);
		// Populate the data into the template view using the data object
		mIcon.setImageDrawable(item.packageIcon);
		mTitle.setText(item.title);
		convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					if(item.taskId==0 || !Util.moveToFront(mContext, item.taskId))
						Util.startApp(mContext, item.packageName);
					popupWin.dismiss();
				}
		});
		// Return the completed view to render on screen
		return convertView;
	}
}