package com.gromstudio.treckar.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gromstudio.treckar.R;

/**
 * <b>This is the base class for displaying alert message.</b>
 * This class offers the possibility to define:
 * <ul>
 * <li>A title (optional)</li>
 * <li>A content message (mandatory)</li>
 * <li>The id of a resource picture, displayed between the title and the message (optional)</li>
 * <li>A custom label for the "OK" button (optional)</li>
 * <li>A popup's listener (optional)</li>
 * </ul>
 * 
 * The popup is initialized using {@link initialize} methods, and displayed calling
 * {@link show}. 
 * 
 * Created by clicmobile on 03/10/13.
 */
public class InformPopup extends BasePopup {

	/**
	 * Class' name
	 */
    public static final String TAG = "com.sncf.ter.nfc.ui_InformPopup";

    
    /**
     * The custom title, or null
     */
    String mTitle;

    /**
     * The popup message
     */
    String mMessage;
    
    /**
     * The custom button's text
     */
    String mButtonText;
    
    /**
     * The id of the picture, or 0
     */
    int mImageResource;

    
    /**
     * Default fragment constructor without parameter
     */
    public InformPopup() {
    	mTitle = "";
    	mMessage = "";
    	mButtonText = "Ok";
    	mImageResource = 0;
    }
    
    /**
     * <b>Initializes the popup</b>
     * @param title the popup title, can be null or empty
     * @param message the popup message, cannot be null nor empty
     * @param buttonText the popup label on OK button, if null use default
     * @param listener the popup's listener
     * @return this
     */
    public InformPopup initialize(String title, String message, String buttonText, PopupListener listener) {
        return initialize(title, message, buttonText, 0, listener);
    }
    
    /**
     * <b>Initializes the popup</b>
     * @param title the popup title, can be null or empty
     * @param message the popup message, cannot be null nor empty
     * @param buttonText the popup label on OK button, if null use default
     * @param pictoRes the resource id of an image to display between the title and the message
     * @param listener the popup's listener
     * @return this
     */
    public InformPopup initialize(String title, String message, String buttonText, int pictoRes, PopupListener listener) {
        
    	setPopupListener(listener);

        mTitle = title;
        mMessage = message;
        mButtonText = buttonText;
        mImageResource = pictoRes;

        return this;
    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return mTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getButtonText() {
        if (TextUtils.isEmpty(mButtonText)) {
            return super.getButtonText();
        }
        return mButtonText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateContent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    	LinearLayout result = new LinearLayout(getActivity());
    	result.setOrientation(LinearLayout.VERTICAL);
    	
    	if ( 0!=mImageResource ) {

    		ImageView imageView = new ImageView(getActivity());
    		imageView.setImageResource(mImageResource);
    		
    		int margin = getActivity().getResources().getDimensionPixelSize(R.dimen.popup_default_margin);
    		LinearLayout.LayoutParams pa = new LinearLayout.LayoutParams(
    				LinearLayout.LayoutParams.WRAP_CONTENT,
    				LinearLayout.LayoutParams.WRAP_CONTENT);
    		pa.gravity = Gravity.CENTER_HORIZONTAL;
    		pa.topMargin = margin;
    		pa.bottomMargin = margin;
    		
    		result.addView(imageView, pa);
    		
    	}
    	
    	TextView tv = new TextView(getActivity());
        tv.setText(mMessage);
        tv.setGravity(Gravity.CENTER);
        
		LinearLayout.LayoutParams pt = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		pt.gravity = Gravity.CENTER_HORIZONTAL;

		result.addView(tv, pt);

        return result;
    }


}
