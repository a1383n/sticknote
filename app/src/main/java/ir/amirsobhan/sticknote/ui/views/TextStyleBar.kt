package ir.amirsobhan.sticknote.ui.views

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.ViewTextstyleBarBinding
import jp.wasabeef.richeditor.RichEditor


class TextStyleBar(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) , View.OnClickListener{
    private var binding = ViewTextstyleBarBinding.inflate(LayoutInflater.from(context),this,true)
    private lateinit var editor : RichEditor
    private val defaultColor = context.getColor(R.color.secondary_text)
    private val selectedColor = context.getColor(R.color.primary)

    fun setEditor(editor: RichEditor){
        this.editor = editor
        initStyleBar()
    }

    private fun initStyleBar(){
        binding.textStyleUndo.setOnClickListener(this)
        binding.textStyleRedo.setOnClickListener(this)
        binding.textStyleBold.setOnClickListener(this)
        binding.textStyleItalic.setOnClickListener(this)
        binding.textStyleUnderline.setOnClickListener(this)
        binding.textStyleOl.setOnClickListener(this)
        binding.textStyleUl.setOnClickListener(this)
        binding.textStyleHeader.setOnClickListener(this)
        binding.textStyleJusLeft.setOnClickListener(this)
        binding.textStyleJusCenter.setOnClickListener(this)
        binding.textStyleHusRight.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.textStyle_undo -> editor.undo()
            R.id.textStyle_redo -> editor.redo()
            R.id.textStyle_bold -> editor.setBold().also { changeButtonColor(true,v) }
            R.id.textStyle_italic -> editor.setItalic().also { changeButtonColor(true,v) }
            R.id.textStyle_underline -> editor.setUnderline().also { changeButtonColor(true,v) }
            R.id.textStyle_ol -> editor.setNumbers().also { changeButtonColor(true,v) }
            R.id.textStyle_ul -> editor.setBullets().also { changeButtonColor(true,v) }
            R.id.textStyle_header -> {
                var index = 0
                MaterialAlertDialogBuilder(context,R.style.AlertDialogTheme)
                    .setSingleChoiceItems(R.array.heading_list,0){_,i -> index = i + 1 }
                    .setPositiveButton(R.string.ok){_, _ -> editor.setHeading(index)}
                    .show()
            }
            R.id.textStyle_jus_left -> editor.setAlignLeft().also { changeButtonColor(false,v,binding.textStyleJusCenter,binding.textStyleHusRight) }
            R.id.textStyle_jus_center -> editor.setAlignCenter().also { changeButtonColor(false,v,binding.textStyleHusRight,binding.textStyleJusLeft) }
            R.id.textStyle_hus_right -> editor.setAlignRight().also { changeButtonColor(false,v,binding.textStyleJusLeft,binding.textStyleJusCenter) }
        }
    }

    private fun changeButtonColor(bool: Boolean,vararg views : View){
        views.forEach {
            if (getViewColor(it) == defaultColor && bool){
                it.setBackgroundColor(selectedColor)
            }else{
                it.setBackgroundColor(defaultColor)
            }
        }

        if (!bool){
            views[0].setBackgroundColor(selectedColor)
        }
    }

    private fun getViewColor(v : View?) = (v?.background as ColorDrawable).color
}