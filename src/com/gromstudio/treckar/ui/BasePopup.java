package com.gromstudio.treckar.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gromstudio.treckar.R;
import com.gromstudio.treckar.util.Flags;

/**
 * <b>This is the base popup used to display basic content.</b> 
 * <p>
 * It offers a content conatiner, filled in with the result of the abstract 
 * method onCreateContent, a title, and a simple button to dismiss
 * the popup.
 * </p>
 * <p>
 * Customized title, button label are retrived from overriding class through
 * the methods {@link getTitle} and {@link getButtonText}.
 * The content is inflated by the overriding classes using {@link onCreateContent}.
 * </p>
 * <p>
 * The popup also can be customized defining the following flags:
 * <ul>
 * <li>{@link BasePopup#FLAG_CANCELLABLE} if the user can close the popup 
 * using Back Button</li>
 * <li>{@link BasePopup#FLAG_CLOSEABLE} To display a close button (cross 
 * on the top-right corner).</li>
 * </ul>
 * </p>
 */
public abstract class BasePopup extends android.app.DialogFragment implements View.OnClickListener {

	
	/**
	 * This listener interface observes the BasePopup state. It aims 
	 * at alerting the listener that the popup is closed or cancel.
	 * 
	 * Deriving classes can set the results and return it to the listener 
	 * overriding the getResult method.
	 * 
	 */
    public interface PopupListener {
		public void onTerminate(BasePopup popup, Bundle result);
		public void onDismissed(BasePopup popup);
	}


    
    /**
     * Is set, the popup can be canceled using the back buttton
     */
    public static final int FLAG_CANCELLABLE = 0x01;
    
    
    /**
     * Is set, the close button will be visible
     */
    public static final int FLAG_CLOSEABLE = 0x02;

    
    
    
    /**
     * <b>The popup listener</b>, or null if no one is listening
     */
	private PopupListener mPopupListener;

    /**
     * Popup flags, combination of 
     * <ul>
     * <li>InformPopup#FLAG_CANCELLABL</li>
     * <li>InformPopup#FLAG_CLOSEABLE</li>
     * </ul>
     */
    Flags mFlags;
	
	/**
	 * Empty constructor
	 */
	public BasePopup() {
		super();
    	mFlags = new Flags();
    }

	/**
	 * Sets the popup listener
	 * @param listener
	 * @return this
	 */
    public BasePopup setPopupListener(PopupListener listener) {

		mPopupListener = listener;
        return this;

    }
    
    /**
     * This method should be overrided to customize the text of the 
     * close Button.
     * By default, this text is R.string.ok if the popup is added to
     * an activity, or the hardcoded "ok" string if it is not.
     * @return the button text string 
     */
    public String getButtonText() {
        if ( getActivity()!=null ) {
            return getActivity().getString(R.string.ok);
        } else {
            return "ok";
        }
    }
    
    /**
     * This method returns the title of the popup. If the title is empty,
     * then the layout adapts and the title view visibility will be set to
     * View.GONE.
     * @return the title of the popup
     */
    public abstract String getTitle();

    /**
     * The popup lets the popup inflate the content view throught
     * this method. It is called from the onCreateView view, in order
     * to get the content of the popup.
     * 
     * @param inflater the mayout inflater
     * @param container the container view
     * @param savedInstanceState the bundle coantaining the saved instance.
     * @return the popup's content view
     */
    public abstract View onCreateContent(LayoutInflater inflater, ViewGroup container,
                                         Bundle savedInstanceState);

    /**
     * The popup result ared stored in a Bundle and sent to the popup 
     * listener using PopupListener.onTerminate() method.
     * 
     * @return the result data
     */
    public Bundle getResult() {
        return null;
    }


    /**
     * <b>Set the popup flags.</b>
     * If the popup is showing, update the views;
     */
    public BasePopup setFlags(int flags) {
    	
    	mFlags.setFlags(flags);
    	
    	if ( getActivity()!=null && isAdded() ) {
    		
    		//update the view
    		
    		// Cancelable flag
            getDialog().setCanceledOnTouchOutside(mFlags.isFlagSet(FLAG_CANCELLABLE));
            setCancelable(mFlags.isFlagSet(FLAG_CANCELLABLE));

            // Closeable flag
            if ( getView()!=null ) {
            	final View closeBtn = getView().findViewById(R.id.popup_close_btn);
                closeBtn.setVisibility(mFlags.isFlagSet(FLAG_CLOSEABLE) ? View.VISIBLE : View.GONE);
            }
            
    	}
    	return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(R.drawable.dialog_background);

        getDialog().setCanceledOnTouchOutside(mFlags.isFlagSet(FLAG_CANCELLABLE));
        setCancelable(mFlags.isFlagSet(FLAG_CANCELLABLE));
        
        
        final View rootView = inflater.inflate(R.layout.popup_base, null);
        
        // Set the title visibility
        final TextView title = (TextView) rootView.findViewById(R.id.popup_title);
        String tt = getTitle();
        if ( TextUtils.isEmpty(tt) ) {
            title.setVisibility(View.GONE);
        } else {
            title.setVisibility(View.VISIBLE);
            title.setText(tt);
        }

        // The close button visibility is defined by the flag #FLAG_CLOSEABLE
        final View closeBtn = rootView.findViewById(R.id.popup_close_btn);
        closeBtn.setVisibility(mFlags.isFlagSet(FLAG_CLOSEABLE) ? View.VISIBLE : View.GONE);
        closeBtn.setOnClickListener(this);
        
        
        // Create content
        final FrameLayout layoutContent = (FrameLayout) rootView.findViewById(R.id.popup_content);
        final View content = onCreateContent(inflater, layoutContent, savedInstanceState);
        layoutContent.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        
        // OK button
        final Button button = (Button) rootView.findViewById(R.id.popup_button);
        button.setText(getButtonText());
        button.setOnClickListener(this);

        return rootView;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        if ( mPopupListener!=null ) {
            mPopupListener.onDismissed(this);
        }
        super.onCancel(dialog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View view) {
    	
    	if ( view.getId() == R.id.popup_button ) {

    		//Terminate popup
    		if ( mPopupListener!=null ) {
		        mPopupListener.onTerminate(this, getResult());
		    }
		    dismiss();
    	
    	} else if (view.getId() == R.id.popup_close_btn) {
    	
    		//Cancel popup
		    if ( mPopupListener!=null ) {
		        mPopupListener.onDismissed(this);
		    }
		    dismiss();
    		
    	}
    }

    /**
     * The default implementation is overrided, so the popup is shown
     * using the method FragmentTransaction.commitAllowingStateLoss.
     */
    @Override
    public void show(FragmentManager manager, String tag) {

        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();

    }
    
    /**
     * The default implementation is overrided, so the popup is dismissed
     * using the method FragmentTransaction.commitAllowingStateLoss.
     */
    @Override
    public void dismiss() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(this);
        ft.commitAllowingStateLoss();

    }


}
