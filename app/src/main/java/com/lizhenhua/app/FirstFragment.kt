package com.lizhenhua.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lizhenhua.app.databinding.FragmentFirstBinding
import com.lizhenhua.app.operator.HelloAsyncOperator
import com.lizhenhua.app.operator.HelloSyncOperator
import com.lizhenhua.app.operator.WorldAsyncOperator
import com.lizhenhua.app.operator.WorldSyncOperator
import com.lizhenhua.pipeline.Pipeline
import com.lizhenhua.pipeline.operator.SyncOperator

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            Log.d(this.javaClass.simpleName, it.context.getString(R.string.syncAndSync))
            val pipeline: Pipeline<MutableList<String>, MutableList<String>> =
                Pipeline(HelloSyncOperator()).addSyncOperator(WorldSyncOperator())
            pipeline.execute(mutableListOf()) { list->
                printResultList(list)
            }
        }

        binding.buttonSecond.setOnClickListener {
            Log.d(this.javaClass.simpleName, it.context.getString(R.string.asyncAndAsync))
            val pipeline: Pipeline<MutableList<String>, MutableList<String>> =
                Pipeline(HelloAsyncOperator()).addAsyncOperator(WorldAsyncOperator())
            pipeline.execute(mutableListOf()) { list->
                printResultList(list)
            }
        }

        binding.buttonThird.setOnClickListener {
            Log.d(this.javaClass.simpleName, it.context.getString(R.string.syncAndAsync))
            val pipeline: Pipeline<MutableList<String>, MutableList<String>> =
                Pipeline(HelloSyncOperator()).addAsyncOperator(WorldAsyncOperator())
            pipeline.execute(mutableListOf()) { list->
                printResultList(list)
            }
        }
    }

    private fun printResultList(list: MutableList<String>?) {
        Log.d(this.javaClass.simpleName, "${list?.joinToString(",")}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}