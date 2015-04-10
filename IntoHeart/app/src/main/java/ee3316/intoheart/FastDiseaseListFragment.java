package ee3316.intoheart;

/**
 * Created by Vivian on 8/4/15.
 */

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public  class FastDiseaseListFragment extends ListFragment implements FragmentManager.OnBackStackChangedListener{

        private static final String DEBUG_TAG = "FastDiseaseListFragment";
        int mCurPosition = -1;
        boolean mShowTwoFragments;

        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);

            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            String[] fastDiseaseName =  getResources().getStringArray(R.array.fastdiseasename_array);
            setListAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_activated_1,fastDiseaseName));

            View detailFrame = getActivity().findViewById(R.id.disease_detail_fast);
            mShowTwoFragments = detailFrame !=null && detailFrame.getVisibility()== View.VISIBLE;

            if(savedInstanceState != null){
                mCurPosition = savedInstanceState.getInt("curChoice",0);
            }

            if(mShowTwoFragments == true || mCurPosition != -1){
                viewDiseaseInfo(mCurPosition);
            }

            getFragmentManager().addOnBackStackChangedListener(this);

        }

        @Override
        public  void onBackStackChanged() {
            FastDiseaseDetailWebViewFragment details =
                    (FastDiseaseDetailWebViewFragment) getFragmentManager().findFragmentById(R.id.disease_detail_fast);
            if (details != null) {
                mCurPosition = details.getShownIndex();
                getListView().setItemChecked(mCurPosition, true);

                if (!mShowTwoFragments) {
                    viewDiseaseInfo(mCurPosition);
                }
            }
        }

        @Override
        public  void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("curChoice", mCurPosition);
        }

        @Override
        public  void onListItemClick(ListView l, View v, int position, long id){
            viewDiseaseInfo(position);
        }

        void viewDiseaseInfo(int index){
            mCurPosition = index;
            if(mShowTwoFragments == true){
                //Check what fragment is currently shown,replace if needed.
                FastDiseaseDetailWebViewFragment details =
                        (FastDiseaseDetailWebViewFragment)getFragmentManager().findFragmentById(R.id.disease_detail_fast);
                if(details == null || details.getShownIndex() != index){
                    FastDiseaseDetailWebViewFragment newDetails = FastDiseaseDetailWebViewFragment.newInstance(index);

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.disease_detail_fast,newDetails);
                    if(index != -1){
                        String[] fastDiseaseName =  getResources().getStringArray(R.array.fastdiseasename_array);
                        String strBackStackTagName = fastDiseaseName[index];
                        ft.addToBackStack(strBackStackTagName);
                    }
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }
            }else {
                Intent intent = new Intent();
                intent.setClass(getActivity(),FastDiseaseMainActivity.class);
                intent.putExtra("index",index);
                startActivity(intent);
            }
        }





    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

