// screen/courses/CoursesFragment.kt
package com.sun.englishlearning.screen.courses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sun.englishlearning.data.model.Category
import com.sun.englishlearning.databinding.FragmentCoursesBinding
import com.sun.englishlearning.utils.base.BaseFragment

class CoursesFragment : BaseFragment<FragmentCoursesBinding>(), CoursesContract.View {

    private val presenter: CoursesContract.Presenter = CoursesPresenter()
    private lateinit var adapter: CategoryAdapter

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCoursesBinding {
        return FragmentCoursesBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        presenter.attachView(this)
        adapter = CategoryAdapter(emptyList())
        viewBinding.recyclerViewCourses.adapter = adapter
    }

    override fun initData() {
        presenter.loadCategories()
    }

    override fun showLoading() {
        viewBinding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        viewBinding.progressBar.visibility = View.GONE
    }



    override fun showCategories(categories: List<Category>) {
        adapter.updateData(categories)
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }
}
