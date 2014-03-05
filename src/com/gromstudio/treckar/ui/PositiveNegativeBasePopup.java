package com.gromstudio.treckar.ui;

import com.gromstudio.treckar.R;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * <b>This abstract popup offers the classes overriding it the ability to 
 * customize this simple yes/no popup.</b>
 * <p>
 * As the BasePopup does, override the methods {@link getTitle},
 * {@link getPositive()} and {@link getNegative} to set the custom
 * labels. The content is provided by the overriding classes through
 * the method {@link onCreateContent} which is called from the 
 * Fragment's onCreate method
 * </p>
 * 
 * @author clicmobile
 *
 */
public abstract class PositiveNegativeBasePopup extends DialogFragment implements View.OnClickListener {

	/**
	 * Class' name
	 */
    public static final String TAG = "PositiveNegativeBasePopup.activity_loading";

    /**
     * <p>This Listener is used to be alerted of popup's lifecycle</b>
     */
    public interface PositiveNegativePopupListener {
    	
        void onPopupAnswered(PositiveNegativeBasePopup popup, boolean result);
        void onPopupCancel(PositiveNegativeBasePopup popup);
    
    }

    /**
     * The listener
     */
    private PositiveNegativePopupListener mListener;

    
    
    /**
     * Default fragment constructor
     */
    public PositiveNegativeBasePopup() {
        super();
    }

    /**
     * <b>Override this method to define your custom title</b>
     * @return the customized title
     */
    public abstract String getTitle();

    /**
     * <b>Override this method to define your custom positive label</b>
     * @return the customized button's label
     */
    public String getPositive() {
        if ( getActivity()!=null ) {
            return getActivity().getString(R.string.yes);
        } else {
            return "yes";
        }
    }

    /**
     * <b>Override this method to define your custom negative label</b>
     * @return the customized button's label
     */
    public String getNegative() {
        if ( getActivity()!=null ) {
            return getActivity().getString(R.string.no);
        } else {
            return "no";
        }
    }
    
    /**
     * <b>Override this method to give the popup some content</b>
     * @return the content's view
     */
    public abstract View onCreateContent(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState);

    /**
     * <b>Defines the popup's listener</b>
     */
    public PositiveNegativeBasePopup setOnPopupListener(PositiveNegativePopupListener listener) {
        mListener = listener;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * This method will call {@link onCreateContent()} method.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(com.gromstudio.treckar.R.drawable.dialog_background);

        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.popup_positive_negative, null);

        TextView title = (TextView) rootView.findViewById(R.id.popup_title);
        String titleStr = getTitle();
        if ( TextUtils.isEmpty(titleStr) ) {
        	title.setVisibility(View.GONE);
        } else {
        	title.setVisibility(View.VISIBLE);
        	title.setText(titleStr);
        }
        
        FrameLayout layoutContent = (FrameLayout) rootView.findViewById(R.id.popup_content);

        View content = onCreateContent(inflater, layoutContent, savedInstanceState);

		layoutContent.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));


        Button yes = (Button) rootView.findViewById(R.id.popup_yes_button);
        yes.setText(getPositive());
        yes.setOnClickListener(this);

        Button no = (Button) rootView.findViewById(R.id.popup_no_button);
        no.setText(getNegative());
        no.setOnClickListener(this);

        return rootView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        if ( mListener!=null ) {
            mListener.onPopupCancel(this);
        }
        super.onCancel(dialog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View view) {

        if ( view.getId() == R.id.popup_yes_button ||
                view.getId() == R.id.popup_no_button  ) {
            if ( mListener!=null ) {
                mListener.onPopupAnswered(this, view.getId() == R.id.popup_yes_button);
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
