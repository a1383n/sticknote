          package ir.amirsobhan.sticknote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.databinding.FragmentCloudBinding
import ir.amirsobhan.sticknote.ui.activity.AuthActivity
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import org.koin.android.ext.android.inject


          class CloudFragment : Fragment(){
    private val TAG = "CloudFragment"
    private var _binding : FragmentCloudBinding? = null
    private val binding get() = _binding!!
    private val viewModel : CloudViewModel by inject()
    private val auth : FirebaseAuth = Firebase.auth
    private val isUserSignedIn : Boolean get() = auth.currentUser != null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentCloudBinding.inflate(layoutInflater, container, false)

        updateUI()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK){
            Snackbar.make(requireView(),"Welcome ${data?.getStringExtra("name")}",Snackbar.LENGTH_LONG).show()
        }
    }

    private fun updateUI(){
        if (isUserSignedIn){
            binding.signInCard.visibility = View.GONE
            binding.cloudCard.visibility = View.VISIBLE
            binding.lastSync.text = viewModel.getLastSyncDate()

            binding.syncButtom.setOnClickListener {
                binding.syncButtom.isEnabled = false
                viewModel.putNotesToRemote()
                        .addOnCompleteListener {
                            binding.syncButtom.isEnabled = true
                            if (!it.isSuccessful){
                                Toast.makeText(context,it.exception?.message,Toast.LENGTH_LONG).show()
                            }else{
                                binding.lastSync.text = "Last sync: Now"
                            }
                        }
            }

        }else{
            binding.signInCard.visibility = View.VISIBLE
            binding.cloudCard.visibility = View.GONE
            binding.signInBtn.setOnClickListener {
                startActivityForResult(Intent(activity,AuthActivity::class.java),10)
            }
        }
    }
}