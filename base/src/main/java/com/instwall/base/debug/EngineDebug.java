//package com.instwall.base.debug;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//
//import com.instwall.base.R;
//import com.instwall.base.data.DetectFace;
//import com.instwall.base.data.TrackFace;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//import ashy.earl.common.util.L;
//import ashy.earl.common.util.Util;
//
///**
// * Created by AshyEarl on 2015/10/11.
// */
//public class EngineDebug {
//    private FrameLayout mContent;
//    private Context mContext;
//    private static final int COLOR_GRAY = 0xFF757575;
//    private static final int COLOR_RED = 0xFFD50000;
//    private static final int COLOR_BLUE = 0xFF2196F3;
//    private static final int COLOR_ORANGE = 0xFFEF6C00;
//    private static final int COLOR_BROWN = 0xFF795548;
//    private static final int COLOR_GREEN = 0xFF43A047;
//    private static String TAG = "EngineDebug";
//
//    public EngineDebug(FrameLayout content) {
//        mContent = content;
//        mContext = content.getContext();
//    }
//
//
//    public void showDebug() {
//        MDebug.getInstance(mContext).showView(mContent);
//    }
//
//    public void hideDebug() {
//        MDebug.getInstance(mContext).hideView();
//    }
//
//    private interface Debug {
//        void showView(FrameLayout parent);
//
//        void hideView();
//    }
//
//    private static class LogHolder extends RecyclerView.ViewHolder {
//        private TextView mLog;
//
//        public LogHolder(View itemView) {
//            super(itemView);
//            mLog = (TextView) itemView;
//        }
//
//        public void bind(L.UiLog.Log log) {
//            switch (log.level) {
//                case L.DEBUG:
//                    mLog.setTextColor(COLOR_GREEN);
//                    break;
//                case L.ERROR:
//                    mLog.setTextColor(COLOR_RED);
//                    break;
//                case L.INFO:
//                    mLog.setTextColor(COLOR_BROWN);
//                    break;
//                case L.VERBOSE:
//                    mLog.setTextColor(COLOR_GRAY);
//                    break;
//                case L.WARN:
//                    mLog.setTextColor(COLOR_ORANGE);
//                    break;
//            }
//            mLog.setText(log.msg);
//            mLog.setBackgroundColor(getAdapterPosition() % 2 == 0 ? 0x1F000000 : Color.TRANSPARENT);
//        }
//    }
//
//    private static class LogAdapter extends RecyclerView.Adapter<LogHolder> implements
//            L.UiLog.LogUi {
//        private LayoutInflater mInflater;
//        private MyList<L.UiLog.Log> mLogs = new MyList<>();
//        private RecycleScrollHelper mRecycleScrollHelper;
//
//        public LogAdapter(RecyclerView view) {
//            mRecycleScrollHelper = new RecycleScrollHelper(view);
//        }
//
//        @Override
//        public LogHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            if (mInflater == null) {
//                mInflater = LayoutInflater.from(parent.getContext());
//            }
//            View view = mInflater.inflate(R.layout.log_item, parent, false);
//            return new LogHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(LogHolder holder, int position) {
//            holder.bind(mLogs.get(position));
//        }
//
//        @Override
//        public int getItemCount() {
//            return mLogs.size();
//        }
//
//        @Override
//        public void setLogs(List<L.UiLog.Log> logs) {
//            mLogs.clear();
//            if (logs != null) {
//                mLogs.addAll(logs);
//            }
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public void appendLog(L.UiLog.Log log) {
//            mLogs.add(log);
//            notifyItemInserted(mLogs.size() - 1);
//            mRecycleScrollHelper.scrollToEnd();
//        }
//
//        @Override
//        public void headerLogsRemoved(int size) {
//            mLogs.removeRange(0, size);
//            notifyItemRangeRemoved(0, size);
//            mRecycleScrollHelper.scrollToEnd();
//        }
//
//        private static class MyList<T> extends ArrayList<T> {
//            /**
//             *
//             */
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void removeRange(int fromIndex, int toIndex) {
//                super.removeRange(fromIndex, toIndex);
//            }
//        }
//    }
//
//    private static class RecycleScrollHelper extends RecyclerView.OnScrollListener {
//        private RecyclerView mRecyclerView;
//        private boolean mEnableScrollToEnd = true;
//
//        public RecycleScrollHelper(RecyclerView view) {
//            mRecyclerView = view;
//            view.addOnScrollListener(this);
//        }
//
//        @Override
//        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
//                        .getLayoutManager();
//                int visibleItemCount = layoutManager.getChildCount();
//                int totalItemCount = layoutManager.getItemCount();
//                int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
//                boolean enable = (visibleItemCount + pastVisiblesItems) >= totalItemCount;
//                setAutoScrollEnable(enable);
//            } else {
//                // RecyclerView.SCROLL_STATE_SETTLING
//                // RecyclerView.SCROLL_STATE_DRAGGING
//                // User is using.
//                setAutoScrollEnable(false);
//            }
//        }
//
//        private void setAutoScrollEnable(boolean enable) {
//            if (mEnableScrollToEnd == enable)
//                return;
//            mEnableScrollToEnd = enable;
//            Log.d("", "setAutoScrollEnable: " + enable);
//        }
//
//        public void scrollToEnd() {
//            if (!mEnableScrollToEnd)
//                return;
//            mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
//        }
//    }
//
//    private static void blinkView(final View view) {
//        if (view == null)
//            return;
//        view.setVisibility(View.VISIBLE);
//        Animation animation = (Animation) view.getTag();
//        if (animation == null) {
//            animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.log_anim);
//            animation.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    view.setVisibility(View.INVISIBLE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//                }
//            });
//            view.setTag(animation);
//        }
//        view.clearAnimation();
//        view.startAnimation(animation);
//    }
//
//    public static class MDebug implements Debug {
//        private static MDebug sSelf;
//        // For be_close
//        private List<DetectFace> mDataLst;
//        // Ui
//        private View mUiContent;
//        private TextView mTextViewCount;
//        private TextView mTextViewDetectNum;
//        private TextView mTextViewTrackFace;
//        // Clients
//        private RecyclerView mClientContent;
//        private ClientAdapter mClientAdapter;
//        // Ui - log
//        private RecyclerView mLogView;
//
//        public static MDebug getInstance(Context context) {
//            Util.throwIfNotMainThread();
//            if (sSelf == null) {
//                sSelf = new MDebug(context.getApplicationContext());
//            }
//            return sSelf;
//        }
//
//        private MDebug(Context context) {
//            init(context);
//        }
//
//        public void init(Context context) {
//
//        }
//
//        @Override
//        public void showView(FrameLayout parent) {
//            Log.d(TAG, "showView() called with: ");
//            if (mUiContent != null)
//                return;
//            Log.d(TAG, "showView() tep1 ");
//            View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.debug_layout, parent, false);
//            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) content.getLayoutParams();
//            lp.gravity = Gravity.BOTTOM;
//            parent.addView(content, lp);
//            mUiContent = content;
//            mTextViewCount = content.findViewById(R.id.tv_1);
//            mTextViewDetectNum = content.findViewById(R.id.tv_2);
//            mTextViewTrackFace = content.findViewById(R.id.tv_3);
//
//            mClientContent = content.findViewById(R.id.log);
//            mClientContent.setLayoutManager(new LinearLayoutManager(mClientContent.getContext()));
//            mClientAdapter = new ClientAdapter();
//            mClientAdapter.setData(mDataLst);
//            mClientContent.setAdapter(mClientAdapter);
//            //
//            mLogView = content.findViewById(R.id.log);
//            mLogView.setLayoutManager(new LinearLayoutManager(mLogView.getContext()));
//            LogAdapter logAdapter = new LogAdapter(mLogView);
//            L.UiLog.getInstance(null).setLogUi(logAdapter);
//            mLogView.setAdapter(logAdapter);
//        }
//
//        @Override
//        public void hideView() {
//            if (mUiContent == null)
//                return;
//            ViewGroup parent = (ViewGroup) mUiContent.getParent();
//            parent.removeView(mUiContent);
//            mUiContent = null;
//            mTextViewCount = null;
//            mTextViewDetectNum = null;
//            mTextViewTrackFace = null;
//
//            mClientContent.setAdapter(null);
//            mClientContent = null;
//            mClientAdapter = null;
//            //
//            mLogView.setAdapter(null);
//            mLogView = null;
//            L.UiLog.getInstance(null).setLogUi(null);
//        }
//
//
//        public void dataInit(List<DetectFace> infos) {
//            if (infos != null)
//                mDataLst = new ArrayList<>(infos);
//            if (mClientAdapter != null)
//                mClientAdapter.setData(mDataLst);
//        }
//
//        public void trackFaceChange(int detecNum, int allFaceCount, int faceCount, @NonNull DetectFace face) {
//            if (mTextViewCount == null || mTextViewDetectNum == null || mTextViewTrackFace == null) {
//                return;
//            }
//            mTextViewDetectNum.setText("当前帧检测到人脸数 :" + detecNum);
//            mTextViewCount.setText("新面孔数据 :" + faceCount + "当前统计总人流量 :" + allFaceCount);
//            if (face.mRect == null) return;
//            mTextViewTrackFace.setText("当前关注人脸信息 :" + face.mTackID + "区域 :" + face.mRect.toString());
//        }
//
//        private static class ClientVh extends RecyclerView.ViewHolder {
//            TextView aTextView;
//            TextView bTextView;
//            TextView cTextView;
//            TextView dTextView;
//            TextView eTextView;
//            TextView fTextView;
//            TextView gTextView;
//            TextView hTextView;
//
//            public ClientVh(View itemView) {
//                super(itemView);
//                aTextView = itemView.findViewWithTag("a");
//                bTextView = itemView.findViewWithTag("b");
//                cTextView = itemView.findViewWithTag("c");
//                dTextView = itemView.findViewWithTag("d");
//                eTextView = itemView.findViewWithTag("e");
//                fTextView = itemView.findViewWithTag("f");
//                gTextView = itemView.findViewWithTag("g");
//                hTextView = itemView.findViewWithTag("h");
//            }
//
//            public void bind(DetectFace info) {
////                uuidTextView.setText("UUID: " + info.getProximityUUID());
////                majorTextView.setText("Major: " + info.getMajor());
////                minorTextView.setText("Minor: " + info.getMinor());
////                measuredPowerTextView.setText("MPower: " + info.getPower());
////                rssiTextView.setText("RSSI: " + info.getRssi());
//
//            }
//        }
//
//        private static class ClientAdapter extends RecyclerView.Adapter<ClientVh> {
//            private List<DetectFace> datas = new ArrayList<>();
//            private LayoutInflater mInflater;
//
//            public void setData(List<DetectFace> infos) {
//                datas.clear();
//                if (infos != null) datas.addAll(infos);
//                notifyDataSetChanged();
//            }
//
//            @Override
//            public ClientVh onCreateViewHolder(ViewGroup parent, int viewType) {
//                if (mInflater == null) {
//                    mInflater = LayoutInflater.from(parent.getContext());
//                }
//                View view = mInflater.inflate(R.layout.attribute_item, parent, false);
//                return new ClientVh(view);
//            }
//
//            @Override
//            public void onBindViewHolder(ClientVh holder, int position) {
//                holder.bind(datas.get(position));
//            }
//
//            @Override
//            public int getItemCount() {
//                return datas.size();
//            }
//        }
//    }
//
//}
