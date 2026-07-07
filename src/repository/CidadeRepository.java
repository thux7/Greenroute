package repository;
import model.Cidade;

import java.util.ArrayList;

public class CidadeRepository {
    private ArrayList<Cidade> cidades = new ArrayList<>();

    public void cadastrar(Cidade cidade) {
        cidades.add(cidade);
    }

    public Cidade buscarPorId(int id) {
        for (int i = 0; i < cidades.size(); i++) {
            if (cidades.get(i).getId() == id) {
                return cidades.get(i);
            }
        }
        return null;
    }

    public ArrayList<Cidade> listarTodos() {
        return new ArrayList<>(cidades);
    }

    public boolean atualizar(Cidade cidadeAtualizada) {
        for (int i = 0; i < cidades.size(); i++) {
            if (cidades.get(i).getId() == cidadeAtualizada.getId()) {
                cidades.set(i, cidadeAtualizada);
                return true;
            }
        }
        return false;
    }

    public boolean excluir(int id) {
        for (int i = 0; i < cidades.size(); i++) {
            if (cidades.get(i).getId() == id) {
                cidades.remove(i);
                return true;
            }
        }
        return false;
    }
}
