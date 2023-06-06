package com.davidvelez.petday

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.davidvelez.petday.databinding.FragmentHomeBinding
import com.davidvelez.petday.databinding.ItemServiceBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class HomeFragment : Fragment(), HomeAux{

    private lateinit var mBinding: FragmentHomeBinding

    //Declaración del adaptador
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Service, ServiceHolder>
    //Declaración RecyclerView
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mServicesRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Funciones para configurar el adaptador
        setupFirebase()
        setupAdapter()
        setupRecyclerView()
    }

    private fun setupFirebase() {
        //Ruta donde se almacenara la información(nodos)
        mServicesRef = FirebaseDatabase.getInstance().reference.child("services")
    }

    private fun setupAdapter() {
        val query = mServicesRef

        val options = FirebaseRecyclerOptions.Builder<Service>().setQuery(query) {
            val service = it.getValue(Service::class.java)
            service!!.id = it.key!!
            service
        }.build()


        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Service, ServiceHolder>(options) {
            private lateinit var mContext: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceHolder {
                mContext = parent.context

                val view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_service, parent, false)
                return ServiceHolder(view)
            }

            override fun onBindViewHolder(holder: ServiceHolder, position: Int, model: Service) {
                val service = getItem(position)

                with(holder) {
                    setListener(service)

                    with(binding) {
                        tvTitle.text = service.title
                        cbLike.text = service.likeList.keys.size.toString()

                        FirebaseAuth.getInstance().currentUser?.let {
                            binding.cbLike.isChecked = service.likeList
                                .containsKey(it.uid)
                        }
                        Glide.with(mContext)
                            .load(service.photoUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(imgPhoto)

                        btnDelete.visibility = if (model.ownerUid == ServicesApplication.currentUser.uid) {
                                View.VISIBLE
                            } else {
                                View.INVISIBLE
                            }
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                //Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
                Snackbar.make(mBinding.root, error.message, Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupRecyclerView() {
        mLayoutManager = LinearLayoutManager(context)

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }

    override fun goToTop() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }



    private fun deleteService(service: Service) {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialog_delete_title)
                .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                    val storageSnapshotsRef = FirebaseStorage.getInstance().reference
                        .child(ServicesApplication.PATH_SERVICES)
                        .child(ServicesApplication.currentUser.uid)
                        .child(service.id)
                    storageSnapshotsRef.delete().addOnCompleteListener { result ->
                        if (result.isSuccessful){
                            mServicesRef.child(service.id).removeValue()
                        } else {
                            Snackbar.make(mBinding.root, getString(R.string.home_delete_photo_error),
                                Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton(R.string.dialog_delete_cancel, null)
                .show()
        }

    }
//
    private fun setLike(service: Service, checked: Boolean) {
        val myUserRef = mServicesRef.child(service.id)
            .child(ServicesApplication.PROPERTY_LIKE_LIST)
            .child(ServicesApplication.currentUser.uid)

        if (checked) {
            myUserRef.setValue(true)
        } else {
            myUserRef.setValue(null)
        }
    }

     override fun refresh() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }

    //ViewHolder
    inner class ServiceHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemServiceBinding.bind(view)

        fun setListener(service: Service) {
            with(binding) {
                btnDelete.setOnClickListener { deleteService(service) }

                cbLike.setOnCheckedChangeListener { _, checked ->
                    setLike(service, checked)
                }
            }
        }
    }
}