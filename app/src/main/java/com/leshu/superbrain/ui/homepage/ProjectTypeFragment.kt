package com.leshu.superbrain.ui.homepage

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.leshu.superbrain.R
import com.leshu.superbrain.adapter.HomePageAdapter
import com.leshu.superbrain.ui.base.BaseVMFragment
import com.leshu.superbrain.view.loadpage.BasePageStateView
import com.leshu.superbrain.view.loadpage.LoadPageView
import com.leshu.superbrain.view.loadpage.SimpleLoadPageView
import kotlinx.android.synthetic.main.fragment_home_page.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import timber.log.Timber

class ProjectTypeFragment : BaseVMFragment<ProjectViewModel>(), OnLoadMoreListener {
    override fun providerVMClass(): Class<ProjectViewModel>? = ProjectViewModel::class.java
    override fun setLayoutResId(): Int = R.layout.fragment_home_page
    private val cid by lazy { arguments?.getInt(CID) }// cid==0是最新项目 否项目分类
    private val homePageAdapter = HomePageAdapter()
    private val loadPageView: BasePageStateView = SimpleLoadPageView()
    private lateinit var rootView: LoadPageView
    private var i: Int = 0

    companion object {
        private const val CID = "projectCid"
        fun newInstance(cid: Int): ProjectTypeFragment {
            val fragment = ProjectTypeFragment()
            val bundle = Bundle()
            bundle.putInt(CID, cid)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initView() {
        rootView = activity?.let { activity -> loadPageView.getRootView(activity) } as LoadPageView
        rootView.run {
            failTextView().onClick { refresh() }
            noNetTextView().onClick { refresh() }
        }

        ArticleRv.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = homePageAdapter
        }
        homePageAdapter.run {
            loadMoreModule.setOnLoadMoreListener(this@ProjectTypeFragment)
            isAnimationFirstOnly = true
            setAnimationWithDefault(BaseQuickAdapter.AnimationType.ScaleIn)
        }
        refreshLayout.setOnRefreshListener { refresh() }
        refreshLayout.setEnableLoadMore(false)
    }

    override fun startObserve() {
        mViewModel.apply {
            mProjectListModel.observe(this@ProjectTypeFragment, Observer {
                if (it.isRefresh) refreshLayout.finishRefresh(it.showLoading)
                if (it.showEnd) homePageAdapter.loadMoreModule.loadMoreEnd()
                it.loadPageStatus?.value?.let { loadPageStatus ->
                    loadPageView.convert(
                        rootView,
                        loadPageStatus = loadPageStatus
                    )
                    homePageAdapter.setEmptyView(rootView)
                }
                it.showSuccess?.let { list ->
                    homePageAdapter.run {
                        if (it.isRefresh) setList(list) else addData(list)
                        loadMoreModule.isEnableLoadMore = true
                        loadMoreModule.loadMoreComplete()
                    }
                }
                it.showError?.let { errorMsg ->//加载失败处理
                    homePageAdapter.loadMoreModule.loadMoreFail()
                }
            })
        }
    }

    override fun initData() {
        if (i != 0) homePageAdapter.setList(null) //viewpge缓存4个界面 界面重新加载的时候清空数据重新获取
        refresh()
        i++
    }

    private fun refresh() {
        homePageAdapter.loadMoreModule.isEnableLoadMore = false
        cid?.let { mViewModel.loadProjectArticles(true, it) }
    }

    override fun onLoadMore() {
        cid?.let { mViewModel.loadProjectArticles(false, it) }
    }

}