package com.sun.englishlearning.screen.me.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Language

class LanguageSpinnerAdapter(
    context: Context,
    private val languages: List<Language>
) : ArrayAdapter<Language>(context, 0, languages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val language = getItem(position)
        val view = recycledView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_language_spinner, parent, false)

        val ivLanguageFlag = view.findViewById<ImageView>(R.id.iv_language_flag)
        val tvLanguageName = view.findViewById<TextView>(R.id.tv_language_name)

        language?.let {
            ivLanguageFlag.setImageResource(it.flagIcon)
            tvLanguageName.text = it.name
        }

        return view
    }
}

