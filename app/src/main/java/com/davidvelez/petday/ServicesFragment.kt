package com.davidvelez.petday

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.davidvelez.petday.databinding.FragmentServicesBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ServicesFragment : Fragment() {


    private lateinit var mBinding: FragmentServicesBinding
    private lateinit var mServicesStorageRef: StorageReference
    private lateinit var mServicesDatabaseRef: DatabaseReference

    private var mainAux: MainAux? = null
    private var mPhotoSelectedUri: Uri? = null

    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                    mPhotoSelectedUri = it.data?.data

                with(mBinding) {
                    imgPhoto.setImageURI(mPhotoSelectedUri)
                    tilTitle.visibility = View.VISIBLE
                    tvMessage.text = getString(R.string.post_message_valid_title)
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        mBinding = FragmentServicesBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTextField()
        setupButtons()
        setupFirebase()
    }

    private fun setupTextField() {
        with(mBinding) {
            etTitle.addTextChangedListener {validateFields(tilTitle)}
        }
    }

    private fun setupButtons() {
        with(mBinding) {
            btnPost.setOnClickListener { if (validateFields(tilTitle)) postService() }
            btnSelect.setOnClickListener { openGallery() }
        }
    }
    private fun setupFirebase() {
        mServicesStorageRef = FirebaseStorage.getInstance().reference.child(ServicesApplication.PATH_SERVICES)
        mServicesDatabaseRef = FirebaseDatabase.getInstance().reference.child(ServicesApplication.PATH_SERVICES)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainAux = activity as MainAux
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }

    private fun postService() {
        if (mPhotoSelectedUri != null) {
            enableUI(false)
            mBinding.progressBar.visibility = View.VISIBLE

            val key = mServicesDatabaseRef.push().key!!
            val myStorageRef = mServicesStorageRef.child(ServicesApplication.currentUser.uid)
                .child(key)

            myStorageRef.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                    with(mBinding) {
                        progressBar.progress = progress
                        tvMessage.text = String.format("%s%%", progress)
                    }
                }
                .addOnCompleteListener {
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveService(key, downloadUri.toString(), mBinding.etTitle.text.toString().trim())
                    }
                }
                .addOnFailureListener {
                    mainAux?.showMessage(R.string.post_message_post_image_fail)
                }
        }
    }

    private fun saveService(key: String, url: String, title: String) {
        val snapshot = Service(ownerUid = ServicesApplication.currentUser.uid,
            title = title, photoUrl = url)
        mServicesDatabaseRef.child(key).setValue(snapshot)
            .addOnSuccessListener {
                hideKeyboard()
                mainAux?.showMessage(R.string.post_message_post_success)

                with(mBinding) {
                    tilTitle.visibility = View.GONE
                    etTitle.setText("")
                    tilTitle.error = null
                    tvMessage.text = getString(R.string.post_message_title)
                    imgPhoto.setImageDrawable(null)
                }
            }
            .addOnCompleteListener { enableUI(true) }
            .addOnFailureListener { mainAux?.showMessage(R.string.post_message_post_snapshot_fail) }
    }

    private fun validateFields(vararg textFields: TextInputLayout): Boolean {
        var isValid = true

        for (textField in textFields) {
            if (textField.editText?.text.toString().trim().isEmpty()) {
                textField.error = getString(R.string.helper_required)
                isValid = false
            } else textField.error = null
        }

        return isValid
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun enableUI(enable: Boolean) {
        with(mBinding) {
            btnSelect.isEnabled = enable
            btnPost.isEnabled = enable
            tilTitle.isEnabled = enable
        }
    }
}