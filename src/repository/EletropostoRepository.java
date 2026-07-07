package repository;

import model.Eletroposto;

import java.util.ArrayList;

public class EletropostoRepository {
    private ArrayList<Eletroposto> eletropostos = new ArrayList<>();

    public void cadastrar(Eletroposto eletroposto) {
        eletropostos.add(eletroposto);
    }

    public Eletroposto buscarPorId(int id) {
        for (int i = 0; i < eletropostos.size(); i++) {
            if (eletropostos.get(i).getId() == id) {
                return eletropostos.get(i);
            }
        }
        return null;
    }

    public ArrayList<Eletroposto> listarTodos() {
        return new ArrayList<>(eletropostos);
    }

    public boolean atualizar(Eletroposto eletropostoAtualizado) {
        for (int i = 0; i < eletropostos.size(); i++) {
            if (eletropostos.get(i).getId() == eletropostoAtualizado.getId()) {
                eletropostos.set(i, eletropostoAtualizado);
                return true;
            }
        }
        return false;
    }

    public boolean excluir(int id) {
        for (int i = 0; i < eletropostos.size(); i++) {
            if (eletropostos.get(i).getId() == id) {
                eletropostos.remove(i);
                return true;
            }
        }
        return false;
    }

    public ArrayList<Eletroposto> buscarPorCidade(int cidadeId) {
        ArrayList<Eletroposto> resultado = new ArrayList<>();
        for (int i = 0; i < eletropostos.size(); i++) {
            if (eletropostos.get(i).getCidadeId() == cidadeId) {
                resultado.add(eletropostos.get(i));
            }
        }
        return resultado;
    }
}
