package ir.amirsobhan.sticknote.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.RowNoteBinding
import ir.amirsobhan.sticknote.helper.DecryptionResult
import ir.amirsobhan.sticknote.helper.EncryptionHelper
import ir.amirsobhan.sticknote.ui.activity.NoteActivity
import java.text.SimpleDateFormat
import java.util.*


class NoteAdapter(val context: Context?, private val selectedItems : MutableLiveData<MutableList<Note>>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>(){
    private var noteList: MutableList<Note> = mutableListOf()
    private var viewsList : MutableList<NoteViewHolder> = mutableListOf()
    private lateinit var isProgress : MutableLiveData<Boolean>

    class NoteViewHolder(val binding: RowNoteBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note, onClick: () -> Unit, onLongClick : ()-> Unit) {
            binding.root.isVisible = false
            binding.title.text = note.title
            binding.time.text = parseDate(note.timestamp.toDate())

            val html = "<font color='${if (Constants.isDarkMode(context)) "white" else "black"}'>" + note.text + "</font>"
            var isLongClick = false

            binding.body.loadData(html,"text/html", "UTF-8")
            binding.body.setBackgroundColor(Color.TRANSPARENT)
            binding.body.isVerticalScrollBarEnabled = false
            binding.body.isHorizontalScrollBarEnabled = false
            binding.body.setOnLongClickListener {
                onLongClick()
                isLongClick = true
                true
            }

            binding.body.setOnTouchListener { v, event ->
                if (event.action === MotionEvent.ACTION_MOVE) {
                     true
                }

                if (event.action === MotionEvent.ACTION_UP) {
                    if (!isLongClick){
                        onClick()
                    }else{
                        isLongClick = false
                    }
                }
                 false
            }

            // If note body is large show part of that
            if(note.text != null && note.text!!.length > 200){
                binding.body.loadData(context.getString(R.string.note_adapter_long_body,html.substring(0,200)),"text/html", "UTF-8")
            }

            binding.title.isVisible = note.title.isNotEmpty()
        }

        /**
         * Parse to readable format
         * @param date The date you want to convert to string
         * @return The output of <code>formatDate()</code> function
         */
        private fun parseDate(date : Date): String {
            val targetCalendar = Calendar.getInstance()
            val nowCalendar = Calendar.getInstance()

            // Set note date for targetCalendar
            targetCalendar.time = date

            // Find the best date format to show
            return when {
                // When note create in today
                targetCalendar[Calendar.DAY_OF_MONTH] == nowCalendar[Calendar.DAY_OF_MONTH] -> {
                    // Show hour, minute and am or pm
                    formatDate("h:m a",date)
                }
                // When note create in this week
                targetCalendar[Calendar.WEEK_OF_YEAR] == nowCalendar[Calendar.WEEK_OF_YEAR] -> {
                    // Show day of month, hour, minute and am or pm
                    formatDate("E h:m a",date)
                }
                // When note create in this month
                targetCalendar[Calendar.MONTH] == nowCalendar[Calendar.MONTH] -> {
                    // Show month name, day of month, hour, minute and am or pm
                    formatDate("MMMM d, h:m a",date)
                }
                else ->
                // When it take long time to create note
                {
                    // Show month name, day of month and year
                    formatDate("MMMM d, y",date)
                }
            }

        }

        /**
         * @param format The string format to convert Date to it
         * @param date The date you want to convert to string with custom format
         * @return The string of date
         */
        private fun formatDate(format: String, date: Date): String {
            return SimpleDateFormat(format,Locale.getDefault()).format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NoteViewHolder(
        RowNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false),context.let { parent.context }
    )

    override fun onBindViewHolder(holderNote: NoteViewHolder, position: Int) {
        val note = decryptNote(noteList[position])
        holderNote.bind(note,
            { onClick(note, holderNote) }, { onLongClick(note, holderNote) })

        //Collect all views in adapter
        viewsList.add(holderNote)

        if (noteList.size - 1 == position){
            holderNote.binding.body.webViewClient = object : WebViewClient(){
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewsList.forEach { it.binding.root.isVisible = true }
                    isProgress.postValue(false)
                }
            }
        }


        /*
            When user clicked on the note check if we had active selected items this note was selected too
            and the otherwise show the NoteActivity
         */
        holderNote.binding.root.setOnClickListener{ onClick(note, holderNote) }

        /*
            When user longClick on the note we check if had active selected items do nothing
            and the other wise we checked this note as selected
         */
        holderNote.binding.root.setOnLongClickListener { onLongClick(note, holderNote) }

    }

    /**
     * Try to decrypt note with Helper class
     * @param note An encrypted note
     */
    fun decryptNote(note: Note): Note {
        val pair = EncryptionHelper.tryDecrypt(note)

        return when(pair.first){
            DecryptionResult.SUCCESSES -> note
            DecryptionResult.SECRET_NOT_FOUND -> EncryptionHelper.notReadyNote(note)
            else -> note
        }
    }


    /**
     * Set list and notify adapter the data has been changed
     * @param list The list of you want to set in the adapter
     */
    fun setList(list : List<Note>,isProgress: MutableLiveData<Boolean>){
        this.isProgress = isProgress
        noteList = list.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Update selectedItems LiveDate
     */
    private fun updateSelectedItems(note: Note){
        val value = selectedItems.value!!

        // If we already had note in list remove that and the otherwise add it
        if (value.contains(note)){
            value.remove(note)
        }else{
            value.add(note)
        }

        selectedItems.postValue(value)
    }


    /**
     * Uncheck all notes and clear the selectedItems list
     */
    fun unCheckAll(){
        viewsList.forEach {
            it.binding.root.isChecked = false
        }
    }

    override fun getItemCount() = noteList.size

    private fun onClick(note : Note, holderNote : NoteViewHolder) {
        if (selectedItems.value!!.size == 0) {
            //Prepare and Show the note activity
            val intent = Intent(context, NoteActivity::class.java)
            intent.putExtra(Constants.NOTE_ACTIVITY_EXTRA_INPUT, note.id)
            Log.d("NoteAdapter", "bind: ${Gson().toJson(note)}")

            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, note.id)
            }

            context?.startActivity(intent)
        }else{
            // We check if this note has already checked or not
            if (!holderNote.binding.root.isChecked) {
                holderNote.binding.root.apply {
                    isCheckable = true
                    isChecked = true
                }
                updateSelectedItems(note)
            }else{
                holderNote.binding.root.isChecked = false
                updateSelectedItems(note)
            }
        }
    }

    fun onLongClick(note: Note,holderNote: NoteViewHolder): Boolean {
        if (selectedItems.value!!.size == 0){
            updateSelectedItems(note)
        }else{
            return false
        }

        holderNote.binding.root.apply {
            isCheckable = true
            isChecked = true
        }

        return true
    }
}