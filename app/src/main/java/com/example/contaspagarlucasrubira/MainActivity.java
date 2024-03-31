package com.example.contaspagarlucasrubira;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Categoria> categorias;
    private CustomListAdapter adapter;
    private EditText editTextCategoria;
    private static final int REQUEST_EDITAR_CATEGORIA = 1;
    private int indiceCategoriaSelecionada = -1;

    private ListView listViewCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewCategorias = findViewById(R.id.listViewCategorias);
        editTextCategoria = findViewById(R.id.editTextCategoria);
        Button btnAdicionar = findViewById(R.id.btnAdicionar);

        categorias = new ArrayList<>();
        adapter = new CustomListAdapter();
        listViewCategorias.setAdapter(adapter);

        btnAdicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeCategoria = editTextCategoria.getText().toString().trim();
                if (!nomeCategoria.isEmpty()) {
                    Categoria categoria = new Categoria(nomeCategoria);
                    categorias.add(categoria);
                    adapter.notifyDataSetChanged();
                    editTextCategoria.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Você precisa digitar o nome da categoria para adicioná-la", Toast.LENGTH_SHORT).show();
                }
            }
        });

        listViewCategorias.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Categoria categoriaSelecionada = categorias.get(position);
                indiceCategoriaSelecionada = position;

                // Iniciar a EditarContaActivity passando a categoria selecionada como extra
                Intent intent = new Intent(MainActivity.this, EditarContaActivity.class);
                intent.putExtra("posicaoCategoria", position);
                intent.putExtra("categoriaSelecionada", categoriaSelecionada);
                intent.putExtra("despesasCategoria", categoriaSelecionada.getContas());
                startActivityForResult(intent, REQUEST_EDITAR_CATEGORIA);

                return true; // Retorna true para indicar que o clique longo foi consumido
            }
        });
    }

    private class CustomListAdapter extends ArrayAdapter<Categoria> {

        CustomListAdapter() {
            super(MainActivity.this, R.layout.custom_list_item_categoria, categorias);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.custom_list_item_categoria, parent, false);
            }

            Categoria categoria = categorias.get(position);

            ImageView imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            // Configurar o ícone da categoria aqui...

            TextView textViewNome = itemView.findViewById(R.id.textViewNome);
            textViewNome.setText(categoria.getDescricao());

            TextView textViewNumeroContas = itemView.findViewById(R.id.textViewNumeroContas);
            textViewNumeroContas.setVisibility(View.VISIBLE);
            textViewNumeroContas.setText("Número de contas: " + categoria.getContas().size());

            TextView textViewValorTotal = itemView.findViewById(R.id.textViewValorTotal);
            textViewValorTotal.setVisibility(View.VISIBLE);
            double valorTotal = 0;
            for (Conta conta : categoria.getContas()) {
                valorTotal += conta.getValor();
            }
            textViewValorTotal.setText("Valor total: R$ " + String.format("%.2f", valorTotal));

            return itemView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDITAR_CATEGORIA && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("categoriaAtualizada") && data.hasExtra("posicaoCategoria")) {
                Categoria categoriaAtualizada = (Categoria) data.getSerializableExtra("categoriaAtualizada");
                int posicaoCategoria = data.getIntExtra("posicaoCategoria", -1);

                // Verifica se a posição é válida
                if (posicaoCategoria != -1) {
                    // Atualiza a categoria na lista de categorias na posição especificada
                    categorias.set(posicaoCategoria, categoriaAtualizada);
                    adapter.notifyDataSetChanged(); // Notificar o adapter sobre a mudança na lista

                    // Atualiza o nome, número de contas e valor total da categoria na UI
                    View itemView = listViewCategorias.getChildAt(posicaoCategoria);
                    if (itemView != null) {
                        TextView textViewNome = itemView.findViewById(R.id.textViewNome);
                        textViewNome.setText(categoriaAtualizada.getDescricao());

                        TextView textViewNumeroContas = itemView.findViewById(R.id.textViewNumeroContas);
                        textViewNumeroContas.setText("Número de contas: " + categoriaAtualizada.getContas().size());

                        TextView textViewValorTotal = itemView.findViewById(R.id.textViewValorTotal);
                        double valorTotal = 0;
                        for (Conta conta : categoriaAtualizada.getContas()) {
                            valorTotal += conta.getValor();
                        }
                        textViewValorTotal.setText("Valor total: R$ " + String.format("%.2f", valorTotal));
                    }
                }
            }
        }
    }

}
