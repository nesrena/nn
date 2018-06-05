package sa.fadfed.fadfedapp.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.NativeAd;
import com.eightbitlab.supportrenderscriptblur.SupportRenderScriptBlur;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder;
import com.talktoapi.body.PurchaseBody;
import com.talktoapi.callback.PremiumPurchaseCallback;
import com.tapjoy.TapjoyConstants;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.leolin.shortcutbadger.ShortcutBadger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import sa.fadfed.fadfedapp.BuildConfig;
import sa.fadfed.fadfedapp.C6042R;
import sa.fadfed.fadfedapp.FadFedApplication;
import sa.fadfed.fadfedapp.chat.ChatContract.Presenter;
import sa.fadfed.fadfedapp.chat.domain.item_animator.SpeedyLinearLayoutManager;
import sa.fadfed.fadfedapp.chat.domain.model.ChatMessage;
import sa.fadfed.fadfedapp.chat.domain.model.ReportClasses;
import sa.fadfed.fadfedapp.chat.fragment.GifFragment;
import sa.fadfed.fadfedapp.chat.fragment.GifFragmentListner;
import sa.fadfed.fadfedapp.data.source.ConnectionStateManager;
import sa.fadfed.fadfedapp.data.source.ConnectionStateManager.UserActions;
import sa.fadfed.fadfedapp.data.source.DatabaseRepository;
import sa.fadfed.fadfedapp.data.source.events.ForegroundState;
import sa.fadfed.fadfedapp.data.source.events.PremiumPurchaseFragmentState;
import sa.fadfed.fadfedapp.data.source.events.SearchNewChat;
import sa.fadfed.fadfedapp.data.source.model.SocketMessageIncoming.SessionData;
import sa.fadfed.fadfedapp.data.source.model.SocketMessageIncoming.StatusData;
import sa.fadfed.fadfedapp.data.source.model.SocketMessageIncoming.SyncData;
import sa.fadfed.fadfedapp.home.fragment.PremiumPurchasePresenter;
import sa.fadfed.fadfedapp.util.ActivityUtils;
import sa.fadfed.fadfedapp.util.AnalyticsManager;
import sa.fadfed.fadfedapp.util.FadFedLog;
import sa.fadfed.fadfedapp.util.GeneralUtils;
import sa.fadfed.fadfedapp.util.NetworkChangeReceiver;
import sa.fadfed.fadfedapp.util.NetworkChangeReceiver$NetworkChangeListner;
import sa.fadfed.fadfedapp.util.ad_utils.AdManager;
import sa.fadfed.fadfedapp.util.billing_utils.IabHelper;
import sa.fadfed.fadfedapp.util.custom_views.DotProgressBar;
import ui.home.fragment.PremiumPurchaseFragment;

public abstract class ChatActivity extends AppCompatActivity implements ChatContract$View, ChatAdapter$MessageEventListner, NetworkChangeReceiver$NetworkChangeListner {
    public static String TIMESTAMP_KEY = "timestampKey";
    private int AD_COUNT = 5;
    private long AD_MINUTES = TapjoyConstants.PAID_APP_TIME;
    private String AD_TYPE = "BottomBanner";
    private int CHAT_GROUP = -1;
    private boolean CONTINUE_START_NEW_MATCH = false;
    private int HELP_CLICKER = 0;
    private int IAP_OFFER = 5;
    private String MATCH_ALGO = "Basic";
    private long ON_RESUME_CHAT_TIMESTAMP = System.currentTimeMillis();
    private boolean STOP_AUTO_SCROLLING;
    private String TAG = ChatActivity.class.getSimpleName();
    private boolean USER_TYPING = false;
    private boolean active = false;
    private String[] adTypes = new String[]{"TopNative", "TopBanner", "BottomNative", "BottomBanner"};
    private List<String> algosList = new ArrayList();
    private int appMatchCounts = 0;
    private BannerView appodealBannerTopView;
    private BannerView appodealBannerView;
    private FrameLayout contaierView;
    private FrameLayout contaierViewFull;
    private Toolbar copyToolbar;
    private int deliveredMessageTone;
    String deviceId;
    private FloatingActionButton fab;
    private ImageView gifButton;
    private GifFragmentListner gifScreenListner;
    private Runnable gifSearchRunnable = new 1(this);
    private Handler gifSearchtextHandler = new Handler();
    private Handler goBackHandler;
    private Runnable goBackRunnable;
    private Handler hideTypingHandler;
    private Runnable hideTypingRunnable;
    private IntentFilter inFil;
    private boolean isBannerLoaded = false;
    private boolean isNativeLoaded = false;
    private Handler logHandler;
    private ChatAdapter mChatAdapter;
    private RecyclerView mChatListView;
    private ChatPresenter mChatPresenter;
    private EditText mEditTextMessage;
    private BlurView mLoaderLayout;
    private RelativeLayout mLogLayout;
    private TextView mLogMessage;
    private Runnable mLogRunnable;
    private SpeedyLinearLayoutManager mManager;
    private Presenter mPresenter;
    private FirebaseRemoteConfig mRemoteConfig;
    private TextView mSessionText;
    private SoundPool mSoundPool;
    private String mTimestamp;
    private DotProgressBar mTypingIndicator;
    private RelativeLayout nativeViewBottom;
    private RelativeLayout nativeViewTop;
    private NetworkChangeReceiver networkChangeReceiver;
    private Handler pendingMessageHandler;
    private Runnable pendingMessageRunnable;
    private PremiumPurchaseFragment premiumPurchaseFragment;
    private boolean premiumPurchaseFragmentVisibility;
    private PremiumPurchasePresenter premiumPurchasePresenter;
    private ArrayList<ReportClasses> reportLabels = new ArrayList();
    private RelativeLayout scrollDownButton;
    private boolean searchViewVisibility = true;
    private int sentMessageTone;
    String sessionId;
    TextWatcher textWatcher = new 24(this);
    String token;
    private int totalMatchCount = 0;
    private Handler typingHandler;
    private Runnable typingRunnable;
    private int unreadMessageCount = 0;
    private FrameLayout unreadMessageCounter;
    private TextView unreadMessageTextView;

    public void chatDisconnectedGoBackToHome() {
    }

    public void goToHomeError() {
    }

    public void hideLoader(View view) {
    }

    public void messageDeliveredSuccess() {
    }

    public void redirectToHome() {
    }

    protected abstract void startHomeScreenWithInternetError();

    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) C6042R.layout.activity_chat);
        this.sessionId = UUID.randomUUID().toString();
        this.deviceId = GeneralUtils.getUniqueID(this);
        this.token = GeneralUtils.makeTokenWithoutTimestamp(this.sessionId, this.deviceId);
        disableKeyboardFocus(findViewById(C6042R.id.chat_view));
        setupToolbar();
        this.mTimestamp = getTimestampIntent();
        setupViews();
        setupHandlerRunnable();
        this.appMatchCounts = GeneralUtils.getTotalMatchCount(this);
        this.mChatPresenter = new ChatPresenter(this, DatabaseRepository.getInstance());
        this.networkChangeReceiver = new NetworkChangeReceiver();
        this.networkChangeReceiver.addListner(this);
        this.inFil = new IntentFilter();
        this.inFil.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.inFil.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mChatPresenter.setMessageSendingError();
        AnalyticsManager.getInstance().setTracker(((FadFedApplication) getApplication()).getDefaultTracker());
        AnalyticsManager.getInstance().setFirebaseAnalytics(FirebaseAnalytics.getInstance(this));
        AnalyticsManager.getInstance().setSessionCountPerUser(GeneralUtils.getUniqueID(this));
        getRemoteConfigs();
        AdManager.getInstance().disableSomeAdNetwork(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(C6042R.id.toolbar);
        this.copyToolbar = (Toolbar) findViewById(C6042R.id.copyToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle((CharSequence) "");
        toolbar.setSubtitle((CharSequence) "");
    }

    public boolean onSupportNavigateUp() {
        goToHome();
        return true;
    }

    public void onBackPressed() {
        if (!this.premiumPurchaseFragmentVisibility || this.premiumPurchaseFragment == null) {
            if (DatabaseRepository.getInstance().getCopiedMessages().size() > 0) {
                this.mChatPresenter.clearCopiedMessages();
            } else {
                super.onBackPressed();
            }
            return;
        }
        this.premiumPurchaseFragment.closeFragment();
    }

    private void getRemoteConfigs() {
        this.mRemoteConfig = FirebaseRemoteConfig.getInstance();
        this.mRemoteConfig.setConfigSettings(new Builder().setDeveloperModeEnabled(true).build());
        this.mRemoteConfig.fetch(10).addOnCompleteListener(this, new 2(this));
    }

    private String setAdtype(String str) {
        for (String equals : this.adTypes) {
            if (equals.equals(str)) {
                this.AD_TYPE = str;
                return this.AD_TYPE;
            }
        }
        return this.AD_TYPE;
    }

    private void setupMatchAlgo() {
        this.algosList = GeneralUtils.getAlgoList(this);
        Object obj = null;
        for (String equals : this.algosList) {
            if (this.MATCH_ALGO.equals(equals)) {
                obj = 1;
            }
        }
        if (obj == null) {
            this.MATCH_ALGO = "Basic";
        }
        this.mChatPresenter.setAlgo(this.MATCH_ALGO);
        String str = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setupMatchAlgo(");
        stringBuilder.append(this.MATCH_ALGO);
        stringBuilder.append(")");
        FadFedLog.i(str, stringBuilder.toString());
    }

    private void showAppodealAds() {
        FadFedLog.i(this.TAG, "showAppodealAds()");
        this.nativeViewTop = (RelativeLayout) findViewById(C6042R.id.native_item_top);
        this.nativeViewBottom = (RelativeLayout) findViewById(C6042R.id.native_item);
        AdManager.getInstance().disableLocationPermissionCheck();
        AdManager.getInstance().disableWriteExternalStoragePermissionCheck();
        if (this.AD_TYPE.equals("TopBanner")) {
            AdManager.getInstance().setBannerViewId(C6042R.id.appodealTopBannerView);
        } else if (this.AD_TYPE.equals("BottomBanner")) {
            AdManager.getInstance().setBannerViewId(C6042R.id.appodealBannerView);
        }
        if (!GeneralUtils.isUserPremium(this)) {
            String str = this.AD_TYPE;
            boolean z = true;
            int hashCode = str.hashCode();
            if (hashCode != -2037342654) {
                if (hashCode != 1274755265) {
                    if (hashCode != 1618479532) {
                        if (hashCode == 1913900375) {
                            if (str.equals("BottomBanner")) {
                                z = false;
                            }
                        }
                    } else if (str.equals("TopNative")) {
                        z = true;
                    }
                } else if (str.equals("TopBanner")) {
                    z = true;
                }
            } else if (str.equals("BottomNative")) {
                z = true;
            }
            switch (z) {
                case false:
                    FadFedLog.i(this.TAG, "BottomBanner Ad is initialized");
                    AdManager.getInstance().setBannerViewId(C6042R.id.appodealBannerView);
                    AdManager.getInstance().initializeInterAndBanner(this, BuildConfig.APPODEAL_API_KEY);
                    break;
                case true:
                    FadFedLog.i(this.TAG, "BottomNative Ad is initialized");
                    AdManager.getInstance().initializeNative(this, BuildConfig.APPODEAL_API_KEY);
                    AdManager.getInstance().setAutoCacheNative(false);
                    AdManager.getInstance().cacheNative(this, 1);
                    break;
                case true:
                    FadFedLog.i(this.TAG, "TopBanner Ad is initialized");
                    AdManager.getInstance().setBannerViewId(C6042R.id.appodealTopBannerView);
                    AdManager.getInstance().initializeInterAndBanner(this, BuildConfig.APPODEAL_API_KEY);
                    break;
                case true:
                    FadFedLog.i(this.TAG, "TopNative Ad is initialized");
                    AdManager.getInstance().initializeNative(this, BuildConfig.APPODEAL_API_KEY);
                    AdManager.getInstance().setAutoCacheNative(false);
                    AdManager.getInstance().cacheNative(this, 1);
                    break;
                default:
                    break;
            }
        }
        AdManager.getInstance().addBannerCallback(new 3(this));
        AdManager.getInstance().addInterstitialCallback(new 4(this));
        AdManager.getInstance().addNativeCallback(new 5(this));
    }

    private void showBanner(int i) {
        String str = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("showBanner(");
        stringBuilder.append(i);
        stringBuilder.append(")");
        FadFedLog.i(str, stringBuilder.toString());
        int i2 = 0;
        this.appodealBannerView.setVisibility(i == 1 ? 0 : 8);
        BannerView bannerView = this.appodealBannerTopView;
        if (i != 2) {
            i2 = 8;
        }
        bannerView.setVisibility(i2);
        AdManager.getInstance().showBanner(this);
        this.nativeViewTop.setVisibility(8);
        this.nativeViewBottom.setVisibility(8);
    }

    public void showNativeAd(int i) {
        int i2;
        this.appodealBannerView.setVisibility(8);
        this.appodealBannerTopView.setVisibility(8);
        ViewGroup viewGroup = i == 1 ? this.nativeViewBottom : this.nativeViewTop;
        viewGroup.setVisibility(0);
        NativeAd nativeAdView = AdManager.getInstance().getNativeAdView();
        if (i == 1) {
            i2 = C6042R.id.native_ad_sign;
        } else {
            i2 = C6042R.id.native_ad_sign_top;
        }
        ((TextView) viewGroup.findViewById(i2)).setText("Ad");
        if (i == 1) {
            i2 = C6042R.id.native_title;
        } else {
            i2 = C6042R.id.native_title_top;
        }
        TextView textView = (TextView) viewGroup.findViewById(i2);
        textView.setMaxLines(1);
        textView.setEllipsize(TruncateAt.END);
        textView.setText(nativeAdView.getDescription());
        textView.setText(nativeAdView.getTitle());
        if (i == 1) {
            i2 = C6042R.id.native_description;
        } else {
            i2 = C6042R.id.native_description_top;
        }
        textView = (TextView) viewGroup.findViewById(i2);
        textView.setMaxLines(1);
        textView.setEllipsize(TruncateAt.END);
        textView.setText(nativeAdView.getDescription());
        if (i == 1) {
            i2 = C6042R.id.native_rating;
        } else {
            i2 = C6042R.id.native_rating_top;
        }
        RatingBar ratingBar = (RatingBar) viewGroup.findViewById(i2);
        if (nativeAdView.getRating() == 0.0f) {
            ratingBar.setVisibility(4);
        } else {
            ratingBar.setVisibility(0);
            ratingBar.setRating(nativeAdView.getRating());
            ratingBar.setIsIndicator(true);
            ratingBar.setStepSize(0.1f);
        }
        if (i == 1) {
            i2 = C6042R.id.native_cta;
        } else {
            i2 = C6042R.id.native_cta_top;
        }
        ((Button) viewGroup.findViewById(i2)).setText(nativeAdView.getCallToAction());
        if (i == 1) {
            i2 = C6042R.id.native_icon;
        } else {
            i2 = C6042R.id.native_icon_top;
        }
        ((ImageView) viewGroup.findViewById(i2)).setImageBitmap(nativeAdView.getIcon());
        if (i == 1) {
            i2 = C6042R.id.native_image;
        } else {
            i2 = C6042R.id.native_image_top;
        }
        ((ImageView) viewGroup.findViewById(i2)).setImageBitmap(nativeAdView.getImage());
        View providerView = nativeAdView.getProviderView(this);
        if (providerView != null) {
            if (i == 1) {
                i = C6042R.id.native_provider_view;
            } else {
                i = C6042R.id.native_provider_view_top;
            }
            ((FrameLayout) viewGroup.findViewById(i)).addView(providerView);
        }
        nativeAdView.registerViewForInteraction(viewGroup);
        viewGroup.setVisibility(0);
    }

    private void removeBadgeCount() {
        GeneralUtils.updateBadgeCount(this, true);
        ShortcutBadger.removeCount(this);
    }

    private void setupHandlerRunnable() {
        this.pendingMessageHandler = new Handler();
        this.logHandler = new Handler();
        this.mLogRunnable = new 6(this);
        this.hideTypingHandler = new Handler();
        this.hideTypingRunnable = new 7(this);
        this.typingHandler = new Handler();
        this.typingRunnable = new 8(this);
        this.pendingMessageRunnable = new 9(this);
    }

    private void loadSounds() {
        if (VERSION.SDK_INT >= 21) {
            this.mSoundPool = new SoundPool.Builder().setMaxStreams(5).build();
        } else {
            this.mSoundPool = new SoundPool(5, 3, 100);
        }
        this.sentMessageTone = this.mSoundPool.load(getApplicationContext(), C6042R.raw.sent, 1);
        this.deliveredMessageTone = this.mSoundPool.load(getApplicationContext(), C6042R.raw.sent, 1);
    }

    protected void onStop() {
        super.onStop();
        this.active = false;
        this.mPresenter.disconnectChat("ON STOP");
        AnalyticsManager.getInstance().setMatchedCountPerSessions(GeneralUtils.getUniqueID(this), this.totalMatchCount);
        this.logHandler.removeCallbacks(this.mLogRunnable);
        this.hideTypingHandler.removeCallbacks(this.hideTypingRunnable);
        this.typingHandler.removeCallbacks(this.typingRunnable);
        this.mPresenter.stopChat();
        EventBus.getDefault().unregister(this);
        if (this.premiumPurchasePresenter != null) {
            this.premiumPurchasePresenter.onViewClose(this);
        }
    }

    private void unregisterNetworkReceiver() {
        try {
            if (this.networkChangeReceiver != null) {
                unregisterReceiver(this.networkChangeReceiver);
            }
        } catch (Exception e) {
            FadFedLog.e(ChatActivity.class.getName(), e.getMessage());
        }
    }

    private void blurView() {
        View decorView = getWindow().getDecorView();
        ViewGroup viewGroup = (ViewGroup) decorView.findViewById(16908290);
        Drawable background = decorView.getBackground();
        if (VERSION.SDK_INT >= 17) {
            this.mLoaderLayout.setupWith(viewGroup).windowBackground(background).blurAlgorithm(new RenderScriptBlur(this)).blurRadius(2.0f);
        } else {
            this.mLoaderLayout.setupWith(viewGroup).windowBackground(background).blurAlgorithm(new SupportRenderScriptBlur(this)).blurRadius(2.0f);
        }
    }

    public void disableKeyboardFocus(View view) {
        view.setOnTouchListener(new 10(this));
    }

    public void hideSoftKeyboard() {
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void setupViews() {
        blurView();
        this.appodealBannerView = (BannerView) findViewById(C6042R.id.appodealBannerView);
        this.appodealBannerTopView = (BannerView) findViewById(C6042R.id.appodealTopBannerView);
        this.gifButton = (ImageView) findViewById(C6042R.id.gifButton);
        this.fab = (FloatingActionButton) findViewById(C6042R.id.send_message);
        this.contaierView = (FrameLayout) findViewById(C6042R.id.containerLayout);
        this.contaierViewFull = (FrameLayout) findViewById(C6042R.id.containerLayoutFull);
        this.unreadMessageCounter = (FrameLayout) findViewById(C6042R.id.unreadMessageCounter);
        this.unreadMessageTextView = (TextView) findViewById(C6042R.id.unreadMessageTextView);
        this.scrollDownButton = (RelativeLayout) findViewById(C6042R.id.scrollDownButton);
        this.mChatListView = (RecyclerView) findViewById(C6042R.id.chat_view);
        this.mEditTextMessage = (EditText) findViewById(C6042R.id.et_message);
        setTypeEventListner();
        this.mLogLayout = (RelativeLayout) findViewById(C6042R.id.logLayout);
        this.mTypingIndicator = (DotProgressBar) findViewById(C6042R.id.typingIndicator);
        this.mLogMessage = (TextView) findViewById(C6042R.id.logMessage);
        this.mSessionText = (TextView) findViewById(C6042R.id.sessionText);
        this.mChatListView.setItemAnimator(null);
        this.mChatAdapter = new ChatAdapter(DatabaseRepository.getInstance().getChatMessages(), this, this);
        this.mManager = new SpeedyLinearLayoutManager(this);
        this.mManager.setSmoothScrollbarEnabled(true);
        this.mManager.setStackFromEnd(true);
        this.mChatListView.setLayoutManager(this.mManager);
        this.mChatListView.setAdapter(this.mChatAdapter);
        this.mChatListView.scrollToPosition(this.mChatAdapter.getItemCount() > 0 ? this.mChatAdapter.getItemCount() - 1 : 0);
        this.mChatListView.addOnLayoutChangeListener(new 11(this));
        setupChatListViewScrollListner();
        checkPremiumUI();
    }

    public void checkPremiumUI() {
        if (GeneralUtils.isUserPremium(this)) {
            this.appodealBannerView.setVisibility(8);
            this.appodealBannerTopView.setVisibility(8);
            this.gifButton.setVisibility(0);
            this.gifButton.setOnClickListener(new 12(this));
        } else {
            this.gifButton.setVisibility(8);
        }
        this.mChatAdapter.notifyDataSetChanged();
    }

    private void setupChatListViewScrollListner() {
        this.mChatListView.addOnScrollListener(new 13(this));
    }

    private void hideScrollDownButton() {
        this.STOP_AUTO_SCROLLING = false;
        this.unreadMessageCount = 0;
        this.unreadMessageCounter.setVisibility(4);
        this.scrollDownButton.setVisibility(4);
    }

    private void showScrollDownButton() {
        this.STOP_AUTO_SCROLLING = true;
        this.scrollDownButton.setVisibility(0);
    }

    private String getTimestampIntent() {
        this.mLoaderLayout = (BlurView) findViewById(C6042R.id.blurLayout);
        Intent intent = getIntent();
        if (intent == null) {
            this.mLoaderLayout.setVisibility(8);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.currentTimeMillis());
            stringBuilder.append("");
            return stringBuilder.toString();
        }
        String stringExtra = intent.getStringExtra(TIMESTAMP_KEY);
        if (stringExtra != null) {
            return stringExtra;
        }
        this.mLoaderLayout.setVisibility(8);
        stringBuilder = new StringBuilder();
        stringBuilder.append(System.currentTimeMillis());
        stringBuilder.append("");
        return stringBuilder.toString();
    }

    public void addMessageEditTextWatcher() {
        this.mEditTextMessage.addTextChangedListener(this.textWatcher);
    }

    public void removeEditTextWatcher() {
        this.mEditTextMessage.removeTextChangedListener(this.textWatcher);
    }

    public void sendMessageButtonPress(View view) {
        view = this.mEditTextMessage.getText().toString().trim();
        if (this.gifScreenListner != null) {
            onBackPressed();
            this.mEditTextMessage.setText("");
            return;
        }
        if (view.length() > 0) {
            sendMessage(view, false);
        } else {
            this.mEditTextMessage.setText("");
        }
    }

    private void setTypeEventListner() {
        this.mEditTextMessage.addTextChangedListener(new 14(this));
    }

    private void sendMessage(String str, boolean z) {
        this.mEditTextMessage.setText("");
        this.mPresenter.sendMessage(str, z);
        startPendingCheckHandler();
    }

    private void startPendingCheckHandler() {
        this.pendingMessageHandler.removeCallbacks(this.pendingMessageRunnable);
        this.pendingMessageHandler.postDelayed(this.pendingMessageRunnable, 20000);
    }

    public void goToHome() {
        if (System.currentTimeMillis() >= GeneralUtils.getLastMatchedTime(this) + 120000) {
            if (!isChatEnded()) {
                if (!isFinishing()) {
                    new AlertDialog.Builder(this, C6042R.style.dialog_light).setMessage((int) C6042R.string.CancelChatConfirmTitle).setPositiveButton((int) C6042R.string.ReportAlertAcceptBtn, new 15(this)).setNegativeButton((int) C6042R.string.ReportAlertRejectBtn, null).show();
                }
                return;
            }
        }
        AnalyticsManager.getInstance().setChatLength(GeneralUtils.getUniqueID(this), GeneralUtils.getLiveChatLength(this));
        GeneralUtils.setLastMatchedTime(this, 0);
        this.mPresenter.goBackToHome();
    }

    private boolean isChatEnded() {
        return ConnectionStateManager.getInstance().getFadFedState().getUserAction() == UserActions.LEAVE;
    }

    public void blockUser(View view) {
        if (this.reportLabels.size() > null) {
            showReportList();
        } else {
            new AlertDialog.Builder(this, C6042R.style.dialog_light).setTitle((int) C6042R.string.ReportAlertTitle).setMessage((int) C6042R.string.ReportAlertBody).setPositiveButton((int) C6042R.string.ReportAlertAcceptBtn, new 16(this)).setNegativeButton((int) C6042R.string.ReportAlertRejectBtn, null).show();
        }
    }

    public void handleNewMatchClick(View view) {
        if (this.active == null || isChatEnded() != null || System.currentTimeMillis() <= GeneralUtils.getLastMatchedTime(this) + 120000) {
            checkIAPorAdsBeforeStart();
        } else {
            new AlertDialog.Builder(this, C6042R.style.dialog_light).setMessage(getResources().getString(C6042R.string.alert_chat_close_title)).setPositiveButton((int) C6042R.string.ReportAlertAcceptBtn, new 17(this)).setNegativeButton((int) C6042R.string.ReportAlertRejectBtn, null).show();
        }
    }

    private void checkIAPorAdsBeforeStart() {
        this.mChatPresenter.leaveChat();
        if (this.IAP_OFFER != 0 && this.appMatchCounts % this.IAP_OFFER == 0 && !GeneralUtils.isUserPremium(this)) {
            showIAPScreen();
            this.CONTINUE_START_NEW_MATCH = true;
            this.mPresenter.deleteOldChat();
        } else if (this.AD_COUNT == 0 || !AdManager.getInstance().isInterstitialLoaded() || this.appMatchCounts == 0 || (!(this.appMatchCounts % this.AD_COUNT == 0 || isLongTimePassed()) || GeneralUtils.isUserPremium(this))) {
            startNewMatch();
        } else {
            AdManager.getInstance().showInterstitial(this);
            GeneralUtils.setLastInterstitialTime(this, System.currentTimeMillis());
            this.CONTINUE_START_NEW_MATCH = true;
            this.mPresenter.deleteOldChat();
        }
    }

    private boolean isLongTimePassed() {
        return System.currentTimeMillis() - GeneralUtils.getLastInterstitialTime(this) > this.AD_MINUTES;
    }

    private void startNewMatch() {
        AnalyticsManager.getInstance().setChatLength(GeneralUtils.getUniqueID(this), GeneralUtils.getLiveChatLength(this));
        GeneralUtils.setLastMatchedTime(this, 0);
        this.mPresenter.startNewMatch();
    }

    protected void onStart() {
        super.onStart();
        this.active = true;
        EventBus.getDefault().register(this);
    }

    private void showUILog(String str) {
        this.mLogLayout.setVisibility(0);
        this.mLogMessage.setText(str);
    }

    public void setPresenter(Presenter presenter) {
        this.mPresenter = presenter;
    }

    public void showConnectingView(boolean z) {
        this.searchViewVisibility = z;
        this.mEditTextMessage.setEnabled(z ^ 1);
        this.gifButton.setEnabled(z ^ 1);
        if (z) {
            hideSoftKeyboard();
        }
        new Handler().postDelayed(new 18(this, z), (long) 0);
    }

    public void connectionSuccess() {
        this.mChatPresenter.checkPendingMessage();
        if (this.CONTINUE_START_NEW_MATCH) {
            startNewMatch();
        }
    }

    public void connectionError(String str) {
        FadFedLog.e(ChatActivity.class.getSimpleName(), str);
    }

    public void onNewMessage() {
        CharSequence charSequence;
        this.unreadMessageCount++;
        TextView textView = this.unreadMessageTextView;
        if (this.unreadMessageCount > 9) {
            charSequence = "9+";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.unreadMessageCount);
            stringBuilder.append("");
            charSequence = stringBuilder.toString();
        }
        textView.setText(charSequence);
        this.unreadMessageCounter.setVisibility(0);
        if (!this.STOP_AUTO_SCROLLING) {
            this.mChatListView.smoothScrollToPosition(this.mChatAdapter.getItemCount() - 1);
        }
    }

    public void messageSentSuccess() {
        this.mChatListView.smoothScrollToPosition(this.mChatAdapter.getItemCount() - 1);
    }

    public void retryConnection() {
        this.mPresenter.retryConnection(UUID.randomUUID().toString(), GeneralUtils.getUniqueID(this), null);
    }

    public void messsageSendingError() {
        this.mChatListView.smoothScrollToPosition(this.mChatAdapter.getItemCount() - 1);
    }

    public void userLeftChat() {
        GeneralUtils.toggleOtherUserPremium(this, false);
        hideSoftKeyboard();
        this.mEditTextMessage.setText("");
        this.mChatListView.smoothScrollToPosition(this.mChatAdapter.getItemCount() - 1);
        this.mEditTextMessage.setEnabled(false);
        this.gifButton.setEnabled(false);
        this.mTypingIndicator.setVisibility(4);
        this.typingHandler.removeCallbacks(this.typingRunnable);
    }

    public void messageDeletedSuccess(int i) {
        this.mChatAdapter.notifyItemRemoved(i);
        this.mChatAdapter.notifyItemRangeChanged(i, this.mChatAdapter.getItemCount());
    }

    public void clearEditText() {
        this.mEditTextMessage.setText("");
    }

    public void retryConnectionSuccess() {
        this.CONTINUE_START_NEW_MATCH = false;
    }

    public void matchFindingError() {
        pauseChatActivity();
        resumeChatActivity();
    }

    public void matchedSuccess() {
        this.totalMatchCount++;
        this.appMatchCounts++;
        this.CONTINUE_START_NEW_MATCH = false;
        String str = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ConnectionStateManager.getInstance().getFadFedState().isOtherUserPremium());
        stringBuilder.append(" ");
        Log.e(str, stringBuilder.toString());
        GeneralUtils.toggleOtherUserPremium(this, ConnectionStateManager.getInstance().getFadFedState().isOtherUserPremium());
        this.mChatAdapter.notifyDataSetChanged();
        setupViews();
    }

    public void clearCopiedMessage() {
        checkCopyList();
    }

    public void showGifSearchScreen() {
        this.contaierView.setVisibility(0);
        if (((GifFragment) getSupportFragmentManager().findFragmentById(C6042R.id.containerLayout)) == null) {
            ActivityUtils.addPopUpFragmentToActivity(getSupportFragmentManager(), GifFragment.newInstance(), C6042R.id.containerLayout, GifFragment.class.getSimpleName());
            return;
        }
        super.onBackPressed();
    }

    public void showIAPScreen() {
        this.contaierViewFull.setVisibility(0);
        this.premiumPurchaseFragment = (PremiumPurchaseFragment) getSupportFragmentManager().findFragmentById(C6042R.id.containerLayoutFull);
        if (this.premiumPurchaseFragment == null) {
            this.premiumPurchaseFragment = PremiumPurchaseFragment.newInstance();
            ActivityUtils.addPopUpFragmentToActivity(getSupportFragmentManager(), this.premiumPurchaseFragment, C6042R.id.containerLayoutFull, PremiumPurchaseFragment.class.getSimpleName());
        }
    }

    public void sendGif(String str) {
        onBackPressed();
        sendMessage(str, true);
    }

    public void setGifSearchViews(GifFragmentListner gifFragmentListner) {
        addMessageEditTextWatcher();
        this.mEditTextMessage.setHint(getResources().getString(C6042R.string.gif_search_hint));
        this.mEditTextMessage.setFocusableInTouchMode(true);
        this.mEditTextMessage.requestFocus();
        new Handler().postDelayed(new 19(this), 100);
        this.fab.setImageResource(C6042R.drawable.ic_cross);
        this.gifScreenListner = gifFragmentListner;
    }

    public void removeGifSearchViews() {
        this.mEditTextMessage.setHint(getResources().getString(C6042R.string.chatFieldHint));
        this.fab.setImageResource(C6042R.drawable.ic_send_btn);
        this.gifScreenListner = null;
        removeEditTextWatcher();
    }

    public void userIsPremium(PremiumPurchaseCallback premiumPurchaseCallback) {
        String str = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("userIsPremium(");
        stringBuilder.append(premiumPurchaseCallback.product);
        stringBuilder.append(",");
        stringBuilder.append(premiumPurchaseCallback.expirationTime);
        stringBuilder.append(")");
        FadFedLog.i(str, stringBuilder.toString());
        GeneralUtils.setUserPremiumExpiryDate(this, premiumPurchaseCallback.expirationTime);
        if (GeneralUtils.isPremiumTimeExpired(this) == null) {
            FadFedLog.i(this.TAG, "isPremiumTimeExpired(false)");
            GeneralUtils.setUserPremium(this, true);
        } else if (GeneralUtils.isRenewChecked(this) == null) {
            FadFedLog.i(this.TAG, "isRenewChecked(false)");
            checkForSubscriptionRenewalIAB();
        } else {
            FadFedLog.i(this.TAG, "setUserPremium(false)");
            GeneralUtils.setUserPremium(this, null);
        }
        checkPremiumUI();
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        String str = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onActivityResult(");
        stringBuilder.append(i);
        stringBuilder.append(",");
        stringBuilder.append(i2);
        stringBuilder.append(",");
        stringBuilder.append(intent);
        Log.d(str, stringBuilder.toString());
        str = this.TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("INTENT VALUE : ");
        stringBuilder.append(intent.getStringExtra(IabHelper.RESPONSE_INAPP_PURCHASE_DATA));
        Log.e(str, stringBuilder.toString());
        if (intent.getStringExtra(IabHelper.RESPONSE_INAPP_PURCHASE_DATA) != null) {
            this.premiumPurchasePresenter.registerPurchaseWithServer(intent.getStringExtra(IabHelper.RESPONSE_INAPP_PURCHASE_DATA), this.deviceId, this.sessionId, this.token);
        }
        if (this.premiumPurchasePresenter == null || this.premiumPurchasePresenter.isHelperAvailable()) {
            if (this.premiumPurchasePresenter == null || !this.premiumPurchasePresenter.isHandleActivityResult(i, i2, intent)) {
                Log.d(this.TAG, "onActivityResult handled by IABUtil.");
            } else {
                super.onActivityResult(i, i2, intent);
            }
        }
    }

    private void checkForSubscriptionRenewalIAB() {
        GeneralUtils.setRenewChecked(this, true);
        this.premiumPurchasePresenter = new PremiumPurchasePresenter(new 20(this));
        this.premiumPurchasePresenter.startBilling(this, BuildConfig.BASE_64_ENCODED_STRING);
    }

    private void restorePurchase(ArrayList<PurchaseBody> arrayList) {
        this.premiumPurchasePresenter.restorePurchaseInBackend(arrayList, this.deviceId, this.token, this.sessionId);
    }

    public void errorOnPremiumCheck(String str) {
        String str2 = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append("");
        Log.e(str2, stringBuilder.toString());
    }

    public void setAlgosList(StatusData statusData) {
        String str = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setAlgosList() : ");
        stringBuilder.append(statusData.algos);
        FadFedLog.i(str, stringBuilder.toString());
        if (statusData != null && statusData.algos != null) {
            GeneralUtils.setAlgoList(this, TextUtils.join(",", statusData.algos));
            AnalyticsManager.getInstance().setUserProperty(this.algosList);
            setupMatchAlgo();
        }
    }

    public void saveReportClasses(SyncData syncData) {
        if (syncData != null && syncData.data != null && syncData.data.data != null) {
            this.mChatPresenter.saveReportLabels(syncData.data.data.classes);
        }
    }

    public void setReportLabels(ArrayList<ReportClasses> arrayList) {
        this.reportLabels = arrayList;
    }

    public void updateBadwordFile(String str) {
        this.mChatPresenter.updateBadWordFile(str);
    }

    public void onError(int i) {
        showUILog(getResources().getString(i));
    }

    public void enableMessageEt(boolean z) {
        if (GeneralUtils.getLastMatchedTime(this) == 0) {
            GeneralUtils.setLastMatchedTime(this, System.currentTimeMillis());
            this.ON_RESUME_CHAT_TIMESTAMP = System.currentTimeMillis();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mChatPresenter.clearCopiedMessages();
    }

    public void sessionData(SessionData sessionData) {
        TextView textView = this.mSessionText;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("state: ");
        stringBuilder.append(sessionData.state);
        stringBuilder.append(", sessionId: ");
        stringBuilder.append(sessionData.sessionId);
        stringBuilder.append(", serverId: ");
        stringBuilder.append(sessionData.serverId);
        textView.setText(stringBuilder.toString());
    }

    public void setTypingIndicator(boolean z) {
        this.mTypingIndicator.setVisibility(0);
        this.hideTypingHandler.removeCallbacks(this.hideTypingRunnable);
        this.hideTypingHandler.postDelayed(this.hideTypingRunnable, 2500);
    }

    public void setMatchedGroupData(StatusData statusData) {
        this.CHAT_GROUP = statusData.matchGroup;
    }

    protected void onResume() {
        super.onResume();
        resumeChatActivity();
    }

    private void resumeChatActivity() {
        if (!this.premiumPurchaseFragmentVisibility) {
            if (this.CONTINUE_START_NEW_MATCH) {
                this.mLoaderLayout.setVisibility(0);
            }
            removeBadgeCount();
            FadFedLog.d("RESUMEPAUSE", "ON RESUME");
            if (GeneralUtils.getLastMatchedTime(this) > 0) {
                this.ON_RESUME_CHAT_TIMESTAMP = System.currentTimeMillis();
            }
            registerReceiver(this.networkChangeReceiver, this.inFil);
            this.mPresenter.initChat();
            connectToWebSocket();
            AdManager.getInstance().resumeBanner(this);
            checkPremiumUserStatus();
            EventBus.getDefault().post(new ForegroundState.Builder().chatActivityForeground(true).build());
        }
    }

    private void checkPremiumUserStatus() {
        String uuid = UUID.randomUUID().toString();
        String uniqueID = GeneralUtils.getUniqueID(this);
        this.mChatPresenter.checkPremiumPurchase(uniqueID, GeneralUtils.makeTokenWithoutTimestamp(uuid, uniqueID), uuid);
    }

    private void connectToWebSocket() {
        this.mPresenter.connectChat(UUID.randomUUID().toString(), GeneralUtils.getUniqueID(this), this.mTimestamp);
    }

    protected void onPause() {
        super.onPause();
        pauseChatActivity();
    }

    private void pauseChatActivity() {
        FadFedLog.d("RESUMEPAUSE", "ON PAUSE");
        GeneralUtils.setLiveChatLength(this, this.ON_RESUME_CHAT_TIMESTAMP);
        unregisterNetworkReceiver();
        this.mTypingIndicator.setVisibility(4);
        this.hideTypingHandler.removeCallbacks(this.hideTypingRunnable);
        this.mPresenter.disconnectChat("ONPAUSE");
        this.mPresenter.stopChat();
        EventBus.getDefault().post(new ForegroundState.Builder().chatActivityForeground(false).build());
        GeneralUtils.setTotalMatchCount(this, this.appMatchCounts);
    }

    public void hideLogUi(View view) {
        view.setVisibility(8);
    }

    public void onMessageCopied(ChatMessage chatMessage) {
        ((Vibrator) getSystemService("vibrator")).vibrate(50);
        this.mChatPresenter.setCopied(chatMessage, new 21(this));
    }

    private void checkCopyList() {
        if (DatabaseRepository.getInstance().getCopiedMessages().size() > 0) {
            this.copyToolbar.setVisibility(0);
            setLightStatusBar(this.copyToolbar);
            return;
        }
        this.copyToolbar.setVisibility(8);
        clearLightStatusBar(this.copyToolbar);
    }

    public void onDelete(int i, ChatMessage chatMessage) {
        this.mPresenter.deleteChatMessage(i, chatMessage);
    }

    public void onResend(int i, ChatMessage chatMessage) {
        String message = chatMessage.getMessage();
        this.mPresenter.deleteChatMessage(i, chatMessage);
        sendMessage(message, chatMessage.isGif());
    }

    public void hideKeyboard() {
        hideSoftKeyboard();
    }

    public void onInternetAvailable() {
        if (!(this.goBackHandler == null || this.goBackRunnable == null)) {
            this.goBackHandler.removeCallbacks(this.goBackRunnable);
            this.CONTINUE_START_NEW_MATCH = true;
        }
        this.mPresenter.internetIsback();
        connectToWebSocket();
        this.mLogLayout.setVisibility(8);
        this.mLogMessage.setText("");
    }

    public void onInternetGone() {
        startInternetCheckTimer();
        this.mPresenter.noInternet();
        this.mPresenter.disconnectChat("NO INTERNET");
        showUILog(getResources().getString(C6042R.string.ConnectionErrorNoInternet));
    }

    private void startInternetCheckTimer() {
        if (!(this.goBackHandler == null || this.goBackRunnable == null)) {
            this.goBackHandler.removeCallbacks(this.goBackRunnable);
        }
        if (this.searchViewVisibility) {
            this.goBackRunnable = new 22(this);
            this.goBackHandler = new Handler();
            this.goBackHandler.postDelayed(this.goBackRunnable, 5000);
        }
    }

    public void showHelpDialog(View view) {
        this.HELP_CLICKER++;
        if (this.HELP_CLICKER > 10) {
            view = GeneralUtils.getUniqueID(this);
            this.HELP_CLICKER = 0;
            AlertDialog.Builder builder = new AlertDialog.Builder(this, C6042R.style.dialog_light);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(view.substring(0, 3));
            stringBuilder.append("");
            stringBuilder.append(view.substring(view.length() - 3, view.length()));
            stringBuilder.append("/");
            stringBuilder.append(ConnectionStateManager.getInstance().getFadFedState().getChatId());
            stringBuilder.append("\nGroup: ");
            stringBuilder.append(this.CHAT_GROUP);
            builder.setTitle(stringBuilder.toString()).setMessage(getResources().getString(C6042R.string.alert_support_message)).setPositiveButton(getResources().getString(C6042R.string.alert_close_btn), new 23(this)).show();
        }
    }

    public void copyMessageAction(View view) {
        CharSequence charSequence = "";
        view = DatabaseRepository.getInstance().getCopiedMessages().iterator();
        while (view.hasNext()) {
            ChatMessage chatMessage = (ChatMessage) view.next();
            String string;
            StringBuilder stringBuilder;
            if (chatMessage.getType() == 0) {
                string = getResources().getString(C6042R.string.myInitialCopyText);
                stringBuilder = new StringBuilder();
                stringBuilder.append(charSequence);
                stringBuilder.append(string);
                stringBuilder.append(" ");
                stringBuilder.append(chatMessage.getMessage());
                stringBuilder.append("\n");
                charSequence = stringBuilder.toString();
            } else {
                string = getResources().getString(C6042R.string.strangerInitialCopyText);
                stringBuilder = new StringBuilder();
                stringBuilder.append(charSequence);
                stringBuilder.append(string);
                stringBuilder.append(" ");
                stringBuilder.append(chatMessage.getMessage());
                stringBuilder.append("\n");
                charSequence = stringBuilder.toString();
            }
        }
        this.mChatPresenter.clearCopiedMessages();
        ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", charSequence));
    }

    public void cancelCopyAction(View view) {
        this.mChatPresenter.clearCopiedMessages();
    }

    public void setLightStatusBar(View view) {
        if (VERSION.SDK_INT >= 23) {
            view.setSystemUiVisibility(view.getSystemUiVisibility() | 8192);
            getWindow().setStatusBarColor(-1);
        }
    }

    public void clearLightStatusBar(View view) {
        if (VERSION.SDK_INT >= 23) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, C6042R.color.colorPrimaryDark));
        }
    }

    public void scrollToLastMessage(View view) {
        hideScrollDownButton();
        this.mChatListView.scrollToPosition(this.mChatAdapter.getItemCount() - 1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SearchNewChat searchNewChat) {
        resumeChatActivity();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PremiumPurchaseFragmentState premiumPurchaseFragmentState) {
        this.premiumPurchaseFragmentVisibility = premiumPurchaseFragmentState.isVisible;
        if (premiumPurchaseFragmentState.userIsPremiumNow != null) {
            checkPremiumUI();
        }
    }

    public void showReportList() {
        ReportDialog reportDialog = new ReportDialog(this, C6042R.style.dialog_light);
        reportDialog.setCancelable(false);
        reportDialog.show();
        reportDialog.setReportItems(this.reportLabels);
        reportDialog.setReportClickListner(new 25(this));
    }
}
