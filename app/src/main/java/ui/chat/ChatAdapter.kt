package ui.chat

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class ChatAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    private val listaFragmentos = mutableListOf<Fragment>()
    private val listaTitulos = mutableListOf<String>()


    override fun getItemCount(): Int {
        return listaFragmentos.size
    }

    override fun createFragment(position: Int): Fragment {
        return listaFragmentos[position]
    }

    fun getTitle(position: Int): String{
        return listaTitulos[position]
    }
    fun addFragment(fragment: Fragment, titulo: String){
        listaFragmentos.add(fragment)
        listaTitulos.add(titulo)
    }
}