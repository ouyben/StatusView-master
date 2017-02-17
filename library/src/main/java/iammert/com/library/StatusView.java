package iammert.com.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mertsimsek on 26/01/17.
 */

public class StatusView extends RelativeLayout {

    private static final int DISMISS_ON_COMPLETE_DELAY = 1000;

    /**
     * Current status of status view
     */
    private Status currentStatus;

    /**
     * Automatically hide when status changed to complete
     */
    private boolean hideOnComplete;

    /**
     * Views for each status
     */
    private View completeView;
    private View errorView;
    private View loadingview;

    private TextView succTv, errorTv, loadTv;

    /**
     * Fade in out animations
     */
    private Animation slideOut;
    private Animation slideIn;

    /**
     * layout inflater
     */
    private LayoutInflater inflater;

    /**
     * Handler
     */
    private Handler handler;

    /**
     * Auto dismiss on complete
     */
    private Runnable autoDismissOnComplete = new Runnable() {
        @Override
        public void run() {
            exitAnimation(getCurrentView(currentStatus));
            handler.removeCallbacks(autoDismissOnComplete);
        }
    };

    public StatusView(Context context) {
        super(context);
        init(context, null, 0, 0, 0);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, 0, 0, 0);
    }

    public StatusView(Context context, int completeLayout, int errorLayout, int loadingLayout) {
        super(context);
        init(context, null, completeLayout, errorLayout, loadingLayout);
    }

    public StatusView(Context context, AttributeSet attrs, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    private void init(Context context, AttributeSet attrs, int completeLayout, int errorLayout, int loadingLayout) {

        /**
         * Load initial values
         */
        currentStatus = Status.IDLE;
        hideOnComplete = true;
        slideIn = AnimationUtils.loadAnimation(context, R.anim.sv_slide_in);
        slideOut = AnimationUtils.loadAnimation(context, R.anim.sv_slide_out);
        inflater = LayoutInflater.from(context);
        handler = new Handler();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.statusview);

        /**
         * get status layout ids
         */
        int completeLayoutId = a.getResourceId(R.styleable.statusview_complete, R.layout.sv_layout_complete);
        int errorLayoutId = a.getResourceId(R.styleable.statusview_error, R.layout.sv_layout_error);
        int loadingLayoutId = a.getResourceId(R.styleable.statusview_loading, R.layout.sv_layout_loading);

        hideOnComplete = a.getBoolean(R.styleable.statusview_dismissOnComplete, true);

        /**
         * inflate layouts
         */
        if (completeLayout == 0) {
            completeView = inflater.inflate(completeLayoutId, null);
            errorView = inflater.inflate(errorLayoutId, null);
            loadingview = inflater.inflate(loadingLayoutId, null);
            if (completeLayoutId == R.layout.sv_layout_complete) {
                succTv = (TextView) completeView.findViewById(R.id.tv_succ);
                errorTv = (TextView) errorView.findViewById(R.id.tv_error);
                loadTv = (TextView) loadingview.findViewById(R.id.tv_loading);
            }
        } else {
            completeView = inflater.inflate(completeLayout, null);
            errorView = inflater.inflate(errorLayout, null);
            loadingview = inflater.inflate(loadingLayout, null);
        }

        /**
         * Default layout params
         */
        completeView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        errorView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loadingview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        /**
         * Add layout to root
         */
        addView(completeView);
        addView(errorView);
        addView(loadingview);

        /**
         * set visibilities of childs
         */
        completeView.setVisibility(View.INVISIBLE);
        errorView.setVisibility(View.INVISIBLE);
        loadingview.setVisibility(View.INVISIBLE);

        a.recycle();
    }

    public void setOnErrorClickListener(OnClickListener onErrorClickListener) {
        errorView.setOnClickListener(onErrorClickListener);
    }

    public void setOnLoadingClickListener(OnClickListener onLoadingClickListener) {
        loadingview.setOnClickListener(onLoadingClickListener);
    }

    public View getErrorView() {
        return errorView;
    }

    public View getCompleteView() {
        return completeView;
    }

    public View getLoadingView() {
        return loadingview;
    }

    public void setStatus(final Status status) {
        if (currentStatus == Status.IDLE) {
            currentStatus = status;
            enterAnimation(getCurrentView(currentStatus));
        } else if (status != Status.IDLE) {
            switchAnimation(getCurrentView(currentStatus), getCurrentView(status));
            currentStatus = status;
        } else {
            exitAnimation(getCurrentView(currentStatus));
        }

        handler.removeCallbacksAndMessages(null);
        if (status == Status.COMPLETE)
            handler.postDelayed(autoDismissOnComplete, DISMISS_ON_COMPLETE_DELAY);
    }

    /**
     * TODO: 设置状态提示文字
     *
     * @param succ
     * @param error
     * @param loading
     */
    public void setStateText(String succ, String error, String loading) {
        if (succTv != null && errorTv != null && loadTv != null) {
            succTv.setText(succ);
            errorTv.setText(error);
            loadTv.setText(loading);
        }
    }

    public void setState2loading(String loading) {
        if (loadTv != null) {
            loadTv.setText(loading);
        }
    }

    public void setState2succ(String succ) {
        if (succTv != null) {
            succTv.setText(succ);
        }
    }

    public void setState2error(String error) {
        if (errorTv != null) {
            errorTv.setText(error);
        }
    }

    private View getCurrentView(Status status) {
        if (status == Status.IDLE)
            return null;
        else if (status == Status.COMPLETE)
            return completeView;
        else if (status == Status.ERROR)
            return errorView;
        else if (status == Status.LOADING)
            return loadingview;
        return null;
    }

    private void switchAnimation(final View exitView, final View enterView) {
        clearAnimation();
        exitView.setVisibility(View.VISIBLE);
        exitView.startAnimation(slideOut);
        slideOut.setAnimationListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                slideOut.setAnimationListener(null);
                exitView.setVisibility(View.INVISIBLE);
                enterView.setVisibility(View.VISIBLE);
                enterView.startAnimation(slideIn);
            }
        });
    }

    private void enterAnimation(View enterView) {
        if (enterView == null)
            return;

        enterView.setVisibility(VISIBLE);
        enterView.startAnimation(slideIn);
    }

    private void exitAnimation(final View exitView) {
        if (exitView == null)
            return;

        exitView.startAnimation(slideOut);
        slideOut.setAnimationListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                currentStatus = Status.IDLE;
                exitView.setVisibility(INVISIBLE);
                slideOut.setAnimationListener(null);
            }
        });
    }
}
