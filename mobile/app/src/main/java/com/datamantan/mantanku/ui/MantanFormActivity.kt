package com.datamantan.mantanku.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.datamantan.mantanku.R
import com.datamantan.mantanku.data.ApiResult
import com.datamantan.mantanku.data.MantanRepository
import com.datamantan.mantanku.databinding.ActivityFormBinding
import kotlinx.coroutines.launch

class MantanFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private val repository = MantanRepository()
    private var editingId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        editingId = intent.getIntExtra(EXTRA_ID, -1)
        val isEdit = editingId != -1

        supportActionBar?.title = if (isEdit) {
            getString(R.string.btn_edit)
        } else {
            getString(R.string.btn_add)
        }

        if (isEdit) {
            binding.etNama.setText(intent.getStringExtra(EXTRA_NAMA))
            binding.etNoHp.setText(intent.getStringExtra(EXTRA_NO_HP))
            binding.etAlamat.setText(intent.getStringExtra(EXTRA_ALAMAT))
        }

        binding.btnSave.setOnClickListener { submitForm(isEdit) }
    }

    private fun submitForm(isEdit: Boolean) {
        val nama = binding.etNama.text?.toString()?.trim().orEmpty()
        val noHp = binding.etNoHp.text?.toString()?.trim().orEmpty()
        val alamat = binding.etAlamat.text?.toString()?.trim().orEmpty()

        binding.tilNama.error = if (nama.isEmpty()) getString(R.string.msg_field_required) else null
        binding.tilNoHp.error = if (noHp.isEmpty()) getString(R.string.msg_field_required) else null

        if (nama.isEmpty() || noHp.isEmpty()) return

        setLoading(true)

        lifecycleScope.launch {
            val result = if (isEdit) {
                repository.update(editingId, nama, noHp, alamat)
            } else {
                repository.create(nama, noHp, alamat)
            }

            setLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(
                        this@MantanFormActivity,
                        if (isEdit) "Data berhasil diperbarui" else "Data berhasil ditambahkan",
                        Toast.LENGTH_SHORT,
                    ).show()
                    finish()
                }
                is ApiResult.Error -> {
                    Toast.makeText(this@MantanFormActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_NO_HP = "extra_no_hp"
        const val EXTRA_ALAMAT = "extra_alamat"
    }
}
