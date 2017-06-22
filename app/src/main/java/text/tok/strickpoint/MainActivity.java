package text.tok.strickpoint;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import text.tok.strickpoint.view.StrickPonitHelper;

public class MainActivity extends AppCompatActivity {

    private ListView lv;
    private MyAdapter myAdapter;
    private Context mContext=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lv);
        myAdapter = new MyAdapter();
        lv.setAdapter(myAdapter);
    }

    class MyAdapter extends BaseAdapter {
        /**
         * 用于记录需要删除的view的position
         */
        ArrayList<Integer> removeList =new ArrayList<Integer>();
        @Override
        public int getCount() {
            return 60;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if(convertView==null){
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
                viewHolder.mDragView = (TextView) convertView.findViewById(R.id.mDragView);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if(removeList.contains(position)){
                viewHolder.mDragView.setVisibility(View.GONE);
            }
            else {
                viewHolder.mDragView.setVisibility(View.VISIBLE);
                viewHolder.mDragView.setText(String.valueOf(position*3+1));
            }

            /**
             * 注意对于需要实现拖拽效果的view需要单独指定一个布局文件，并且次布局最好不能有viewGroup，
             * 否则view上面显示的文字可能在拖拽时不能识别，这样一是为了方便，二是为了减少消耗
             * 布局方式请参考xml文件
             */

            boolean flag = position%2==1?true:false;
            StrickPonitHelper strickPonitHelper = new StrickPonitHelper(mContext, viewHolder.mDragView,R.layout.includeview,flag);
            //不需要不可拖动功能，可使用以下初始化
           // StrickPonitHelper strickPonitHelper = new StrickPonitHelper(mContext, viewHolder.mDragView,R.layout.includeview);
//            如果你还想监听拖拽过程的坐标变化，你可以在创建对象的时候这样：
           /* new StrickPonitHelper(mContext, viewHolder.mDragView,R.layout.includeview){
                @Override
                public void inRangeMove(PointF dragCanterPoint) {
                    super.inRangeMove(dragCanterPoint);
//                   DoSomething

                }

                @Override
                public void outRangeMove(PointF dragCanterPoint) {
                    super.outRangeMove(dragCanterPoint);
//                   DoSomething
                }
//                ......
            };*/
            setViewOut2InRangeUp(strickPonitHelper);
            setViewOutRangeUp(position, strickPonitHelper);
            setViewInRangeUp(strickPonitHelper);
            setViewInRangeMove(strickPonitHelper);
            setViewOutRangeMove(strickPonitHelper);
            return convertView;
        }

        /**
         * view在范围外移动执行此Runnable
         * @param strickPonitHelper
         */
        private void setViewOutRangeMove(StrickPonitHelper strickPonitHelper) {
            strickPonitHelper.setViewOutRangeMoveRun(new Runnable() {
                @Override
                public void run() {
                   // DisplayUtils.showToast(mContext, "ViewOutRangeMove");
                }
            });
        }

        /**
         * view在范围内移动指此此Runnable
         * @param strickPonitHelper
         */
        private void setViewInRangeMove(StrickPonitHelper strickPonitHelper) {
            strickPonitHelper.setViewInRangeMoveRun(new Runnable() {
                @Override
                public void run() {
                   // DisplayUtils.showToast(mContext, "ViewInRangeMove");
                }
            });
        }

        /**
         * view没有移出过范围，在范围内松手
         * @param strickPonitHelper
         */
        private void setViewInRangeUp(StrickPonitHelper strickPonitHelper) {
            strickPonitHelper.setViewInRangeUpRun(new Runnable() {
                @Override
                public void run() {
                   // DisplayUtils.showToast(mContext, "ViewInRangeUp");
                    myAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * view移出范围，最后在范围外松手
         * @param position
         * @param strickPonitHelper
         */
        private void setViewOutRangeUp(final int position, StrickPonitHelper strickPonitHelper) {
            strickPonitHelper.setViewOutRangeUpRun(new Runnable() {
                @Override
                public void run() {
                   // DisplayUtils.showToast(mContext, "ViewOutRangeUp");
                    removeList.add(position);
                    myAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * view移出过范围，最后在范围内松手执行次Runnable
         * @param strickPonitHelper
         */
        private void setViewOut2InRangeUp(StrickPonitHelper strickPonitHelper) {
            strickPonitHelper.setViewOut2InRangeUpRun(new Runnable() {
                @Override
                public void run() {
                  //  DisplayUtils.showToast(mContext, "ViewOut2InRangeUp");
                    myAdapter.notifyDataSetChanged();
                }
            });
        }

    }
    static class ViewHolder{
        TextView mDragView;
    }
}
