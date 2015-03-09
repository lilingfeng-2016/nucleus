package nucleus.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import nucleus.presenter.Presenter;
import nucleus.manager.PresenterManager;

/**
 * This view is an example of how a view should control it's presenter.
 * You can inherit from this class or copy/paste this class's code to
 * create your own view implementation.
 *
 * @param <PresenterType> a type of presenter to return with {@link #getPresenter}.
 */
public abstract class NucleusLayout<PresenterType extends Presenter> extends FrameLayout {

    private static final String PRESENTER_STATE_KEY = "presenter_state";
    private static final String PARENT_STATE_KEY = "parent_state";

    private PresenterType presenter;
    private Activity activity;
    private OnDropViewAction onDropViewAction = OnDropViewAction.DESTROY_PRESENTER_IF_FINISHING;

    public NucleusLayout(Context context) {
        super(context);
    }

    public NucleusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NucleusLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Returns a current attached presenter.
     * This method is guaranteed to return a non-null value between
     * onAttachedToWindow/onDetachedFromWindow calls.
     *
     * @return a current attached presenter or null.
     */
    public PresenterType getPresenter() {
        return presenter;
    }

    /**
     * Sets an action that should be performed during onDetachedFromWindow call.
     *
     * @param onDropViewAction the action to perform.
     */
    public void setOnDropViewAction(OnDropViewAction onDropViewAction) {
        this.onDropViewAction = onDropViewAction;
    }

    /**
     * Destroys a presenter that is currently attached to the view.
     * Use this method if you set {@link #setOnDropViewAction(OnDropViewAction)} to
     * {@link OnDropViewAction#NONE}.
     */
    public void destroyPresenter() {
        if (presenter != null) {
            presenter.destroy();
            presenter = null;
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;
        super.onRestoreInstanceState(bundle.getParcelable(PARENT_STATE_KEY));
        presenter = PresenterManager.getInstance().provide(this, bundle.getBundle(PRESENTER_STATE_KEY));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            activity = (Activity)getContext();
            if (presenter == null)
                presenter = PresenterManager.getInstance().provide(this, null);
            //noinspection unchecked
            presenter.takeView(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView();
        if (onDropViewAction == OnDropViewAction.DESTROY_PRESENTER ||
            (onDropViewAction == OnDropViewAction.DESTROY_PRESENTER_IF_FINISHING && activity.isFinishing()))
            destroyPresenter();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putBundle(PRESENTER_STATE_KEY, PresenterManager.getInstance().save(presenter));
        bundle.putParcelable(PARENT_STATE_KEY, super.onSaveInstanceState());
        return bundle;
    }
}
