package repository;

import model.Veiculo;

import java.util.ArrayList;

public class VeiculoRepository {
    private ArrayList<Veiculo> veiculos = new ArrayList<>();

    public void cadastrar(Veiculo veiculo){
        veiculos.add(veiculo);
    }

    public Veiculo buscarPorId(int id){
        for (int i = 0; i < veiculos.size(); i++){
            if (veiculos.get(i).getId() == id){
                return veiculos.get(i);
            }
        }
        return null;
    }

    public ArrayList<Veiculo> listarTodos(){
        return new ArrayList<>(veiculos);
    }

    public boolean atualizar(Veiculo veiculoAtualizado) {
        for (int i = 0; i < veiculos.size(); i++) {
            if (veiculos.get(i).getId() == veiculoAtualizado.getId()) {
                veiculos.set(i, veiculoAtualizado);
                return true;
            }
        }
        return false;
    }

    public boolean excluir(int id){
        for (int i = 0; i < veiculos.size(); i++){
            if (veiculos.get(i).getId() == id){
                veiculos.remove(i);
                return true;
            }
        }
        return false;
    }
}
