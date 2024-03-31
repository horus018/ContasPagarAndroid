package com.example.contaspagarlucasrubira;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditarContaActivity extends AppCompatActivity {
    private Categoria categoriaSelecionada;
    private EditText editTextNomeCategoria;

    private EditText editTextDespesa;
    private EditText editTextValor;
    private EditText editTextVencimento;

    private Button btnSalvar;
    private Button btnOk;

    private ArrayList<Conta> despesas;

    private int tamanhoLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_conta_activity);

        editTextNomeCategoria = findViewById(R.id.editTextNomeCategoria);
        editTextDespesa = findViewById(R.id.editTextDespesa);
        editTextValor = findViewById(R.id.editTextValor);
        editTextVencimento = findViewById(R.id.editTextVencimento);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnOk = findViewById(R.id.btnOk);

        // Inicialize a lista de despesas
        despesas = new ArrayList<>();

        // Restante do seu código...

        // Configure o adaptador para a lista de despesas
        ListView listViewDespesas = findViewById(R.id.listViewDespesas);
        CustomListAdapter despesasAdapter = new CustomListAdapter();
        listViewDespesas.setAdapter(despesasAdapter);

        // Recebe a categoria selecionada da Intent
        categoriaSelecionada = (Categoria) getIntent().getSerializableExtra("categoriaSelecionada");

        if (categoriaSelecionada == null) {
            // Se a categoria selecionada for nula, exiba uma mensagem de erro e termine a atividade
            Toast.makeText(this, "Categoria selecionada inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextNomeCategoria.setText(categoriaSelecionada.getDescricao());

        tamanhoLista = categoriaSelecionada.getContas().size();

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descricao = editTextDespesa.getText().toString().trim();
                String valorStr = editTextValor.getText().toString().trim();
                String vencimentoStr = editTextVencimento.getText().toString().trim();
                String novoNomeCategoria = editTextNomeCategoria.getText().toString().trim();

                // Verifica se o nome da categoria está vazio
                if (novoNomeCategoria.isEmpty()) {
                    Toast.makeText(EditarContaActivity.this, "Você precisa informar o nome da categoria para alterá-la", Toast.LENGTH_SHORT).show();
                    return; // Retorna sem fazer mais nada se o nome da categoria estiver vazio
                }

                // Remove os caracteres de formatação do valor
                String cleanValorStr = valorStr.replaceAll("[^\\d.]", "");
                double valor = 0;

                // Parse do valor e vencimento
                if (!cleanValorStr.isEmpty()) {
                    valor = Double.parseDouble(cleanValorStr);
                }
                Date vencimento = parseDate(vencimentoStr); // Implemente o método parseDate conforme necessário

                Conta novaDespesa = null;
                if(tamanhoLista != categoriaSelecionada.getContas().size()){
                    // Cria uma nova despesa
                    novaDespesa = new Conta(descricao, vencimento, valor, categoriaSelecionada);
                    // Adiciona a nova despesa à lista de contas da categoria selecionada
                    categoriaSelecionada.addConta(novaDespesa);
                }

                // Calcula o total das despesas após adicionar a nova despesa
                double totalDespesas = 0;
                for (Conta despesa : categoriaSelecionada.getContas()) {
                    totalDespesas += despesa.getValor();
                }

                categoriaSelecionada.setDescricao(novoNomeCategoria);
                Intent intent = new Intent();

                int posicaoCategoria = getIntent().getIntExtra("posicaoCategoria", -1);
                intent.putExtra("posicaoCategoria", posicaoCategoria);
                intent.putExtra("categoriaAtualizada", categoriaSelecionada);
                intent.putExtra("totalDespesas", totalDespesas); // Passa o total das despesas como um extra
                setResult(RESULT_OK, intent);
                finish(); // Fecha a EditarContaActivity e volta para a MainActivity
            }
        });

        editTextValor.addTextChangedListener(new TextWatcher() {
            DecimalFormat decFormat = new DecimalFormat("#,##0.00");
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    editTextValor.removeTextChangedListener(this);

                    String cleanString = s.toString()
                            .replaceAll("[R$,]", "") // Remove todos os caracteres de R$ e vírgula
                            .replaceAll("[.]", ""); // Remove os pontos de milhares

                    double parsed;
                    try {
                        parsed = Double.parseDouble(cleanString) / 100; // Divide por 100 para obter o valor correto
                    } catch (NumberFormatException e) {
                        parsed = 0.00;
                    }

                    String formatted = decFormat.format(parsed);

                    current = formatted;
                    editTextValor.setText(formatted);
                    editTextValor.setSelection(formatted.length());

                    editTextValor.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editTextValor.removeTextChangedListener(this);

                String cleanString = editable.toString()
                        .replaceAll("[R$,.]", ""); // Remove todos os caracteres de R$, ponto e vírgula

                double parsed;
                try {
                    parsed = Double.parseDouble(cleanString) / 100; // Divide por 100 para obter o valor correto
                } catch (NumberFormatException e) {
                    parsed = 0.00;
                }

                String formatted = decFormat.format(parsed);

                current = formatted;
                editTextValor.setText(formatted);
                editTextValor.setSelection(formatted.length());

                editTextValor.addTextChangedListener(this);
            }
        });

        editTextVencimento.setKeyListener(null); //Pra nao permitir usar o teclado, somente o componente
        editTextVencimento.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Obtém a data atual para definir como data inicial no DatePickerDialog
                    Calendar calendar = Calendar.getInstance();
                    int ano = calendar.get(Calendar.YEAR);
                    int mes = calendar.get(Calendar.MONTH);
                    int dia = calendar.get(Calendar.DAY_OF_MONTH);

                    // Cria uma instância do DatePickerDialog
                    DatePickerDialog datePickerDialog = new DatePickerDialog(EditarContaActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    // Quando o usuário seleciona uma data, atualiza o texto do campo de texto da data de vencimento
                                    String dataSelecionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                                    editTextVencimento.setText(dataSelecionada);
                                }
                            }, ano, mes, dia);

                    // Exibe o DatePickerDialog
                    datePickerDialog.show();
                }
                return false;
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descricao = editTextDespesa.getText().toString().trim();
                String valorStr = editTextValor.getText().toString().trim();
                String vencimentoStr = editTextVencimento.getText().toString().trim();

                // Verifica se todos os campos foram preenchidos
                if (descricao.isEmpty() || valorStr.isEmpty() || vencimentoStr.isEmpty()) {
                    Toast.makeText(EditarContaActivity.this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Remove os caracteres de formatação do valor
                String cleanValorStr = valorStr.replaceAll("[^\\d.]", "");

                // Parse do valor e vencimento
                double valor = Double.parseDouble(cleanValorStr);
                Date vencimento = parseDate(vencimentoStr); // Implemente o método parseDate conforme necessário

                // Cria uma nova despesa
                Conta novaDespesa = new Conta(descricao, vencimento, valor, categoriaSelecionada);
                categoriaSelecionada.addConta(novaDespesa);
                // Adiciona a nova despesa à lista de despesas
                despesas.add(novaDespesa);

                // Notifica o adaptador sobre a mudança na lista de despesas
                despesasAdapter.notifyDataSetChanged();

                // Exibe uma mensagem de sucesso
                Toast.makeText(EditarContaActivity.this, "Despesa adicionada com sucesso", Toast.LENGTH_SHORT).show();

                // Limpa os campos de entrada
                editTextDespesa.setText("");
                editTextValor.setText("");
                editTextVencimento.setText("");

                tamanhoLista = categoriaSelecionada.getContas().size();
            }
        });
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Retorna null se houver erro na conversão
        }
    }

    private class CustomListAdapter extends ArrayAdapter<Conta> {

        CustomListAdapter() {
            super(EditarContaActivity.this, R.layout.custom_list_item_despesa, despesas);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.custom_list_item_despesa, parent, false);
            }

            Conta despesa = getItem(position);

            // Configurar os campos da despesa na UI
            TextView textViewNomeDespesa = itemView.findViewById(R.id.textViewNomeDespesa);
            textViewNomeDespesa.setText(despesa.getDescricao());

            TextView textViewValorDespesa = itemView.findViewById(R.id.textViewValorDespesa);
            textViewValorDespesa.setText("Valor: R$ " + String.format("%.2f", despesa.getValor()));

            TextView textViewVencimentoDespesa = itemView.findViewById(R.id.textViewDataVencimento);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(despesa.getVencimento());
            textViewVencimentoDespesa.setText("Vencimento: " + formattedDate);

            return itemView;
        }
    }

}