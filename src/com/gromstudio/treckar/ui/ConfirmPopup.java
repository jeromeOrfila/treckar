package com.gromstudio.treckar.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gromstudio.treckar.R;

/**
 * <b>This is the base class for displaying a question to the user and get its 
 * positive/negative answer.</b>
 * <p>
 * This class offers the possibility to define:
 * <ul>
 * <li>A title (optional)</li>
 * <li>A content message (mandatory)</li>
 * <li>A custom label for the positive button (optional)</li>
 * <li>A custom label for the negative button (optional)</li>
 * <li>A popup's listener (optional)</li>
 * </ul>
 * </p>
 * 
 * The popup is initialized using {@link initialize} methods, and displayed calling
 * {@link show}. 
 * 
 * Created by clicmobile on 03/10/13.
 */
@SuppressLint("ValidFragment")
public class ConfirmPopup extends PositiveNegativeBasePopup implements View.OnClickListener {

    public static final String TAG = "com.sncf.ter.nfc.ConfirmPopup";

    private final String mTitle;
    private final String mMessage;
    private final String mPositive;
    private final String mNegative;

    /**
     * Constructor
     * @param title the custom title
     * @param message the message
     * @param message
     */
    public ConfirmPopup(String title, String message) {
        super();

        mTitle = title;
        mMessage = message;
        mPositive = null;
        mNegative = null;

    }

    /**
     * Constructor
     * @param title the custom title
     * @param message the message
     * @param positive the positive button text
     * @param negative the negative button text
     */
	public ConfirmPopup(String title, String message, String positive, String negative) {
		super();

        mTitle = title;
        mMessage = message;
        mPositive = positive;
        mNegative = negative;

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
	public String getPositive() {
        if ( TextUtils.isEmpty(mPositive) ) {
            return super.getPositive();
        }
        return mPositive;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
	public String getNegative() {
        if ( TextUtils.isEmpty(mNegative) ) {
            return super.getNegative();
        }
        return mNegative;
    }

    /**
     * <b>The content of this {@link ConfirmPopup} is a single {@link BaseTextView} containing
     * the message</b>
     */
    @Override
	public View onCreateContent(LayoutInflater inflater, ViewGroup container,
                                         Bundle savedInstanceState) {

    	TextView tv = new TextView(getActivity());
        tv.setText(mMessage);
		tv.setGravity(Gravity.CENTER);

        return tv;

    }

}
