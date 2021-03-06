package br.fundatec.magojota.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.fundatec.magojota.converter.ConverterCarrinho;
import br.fundatec.magojota.converter.ConverterProduto;
import br.fundatec.magojota.dao.MagoJotaDao;
import br.fundatec.magojota.dao.ProdutoEntity;

@Component
public class MagoJotaService {

	private MagoJotaDao mDao;

	@Autowired
	public MagoJotaService(MagoJotaDao pDao) {
		this.mDao = pDao;
	}

	public ProdutoBo save(ProdutoBo pBo) {
		try {
			caposValidos(pBo);
			ProdutoEntity pe = ConverterProduto.convertProdutoBoToEntity(pBo);
			pe = mDao.pustProduto(pe);
			pBo = ConverterProduto.convertProdutoEntityToBo(pe);
			return pBo;
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	private void caposValidos(ProdutoBo pBo) {
		if (pBo.getNome() == null || pBo.getNome().trim().isEmpty() || pBo.getValor() == 0) {
			throw new RuntimeException();
		}
	}

	public ProdutoBo update(ProdutoBo pbo, long id) {
		try {
			ProdutoBo boBanco = ConverterProduto.convertProdutoEntityToBo(mDao.getProdutoId(id));
			boBanco = atualizaAtributos(boBanco, pbo);
			mDao.pustProduto(ConverterProduto.convertProdutoBoToEntity(boBanco));
			return boBanco;
		} catch (RuntimeException e) {
			throw new RuntimeException();
		}
	}

	private ProdutoBo atualizaAtributos(ProdutoBo boBanco, ProdutoBo pbo) {
		long id = boBanco.getId();
		boBanco = pbo;
		boBanco.setId(id);
		return boBanco;
	}

	public void delete(long id) {
		removeProdutoDosCarrinhos(id);
		mDao.deleteProduto(id);
	}

	public List<ProdutoBo> getProdutos() {
		List<ProdutoEntity> entities = mDao.getProdutos();
		List<ProdutoBo> bos = ConverterProduto.convertListProdutoEntityToBo(entities);
		return bos;
	}

	public ProdutoBo getProdutoId(long id) {
		ProdutoEntity entity = mDao.getProdutoId(id);
		return ConverterProduto.convertProdutoEntityToBo(entity);
	}

	// =================================================================\\

	public CarrinhoBo getCarrinho() {
		if (mDao.getCarrinho() == null) {
			mDao.createCarrinho();
		}
		return ConverterCarrinho.convertEntityToBo(mDao.getCarrinho());
	}

	public void addProduto(long id) {
		CarrinhoBo cBo = getCarrinho();
		cBo.getProdutos().add(getProdutoId(id));
		atualizaCarrinho(cBo);
	}

	public CarrinhoBo deleteProdutoCarrinho(long id) {
		CarrinhoBo cBo = getCarrinho();
		List<ProdutoBo> bos = cBo.getProdutos();
		int index = 0;
		boolean removeu = false;
		for (ProdutoBo produtoBo : bos) {
			if (produtoBo.getId() == id) {
				cBo.getProdutos().remove(index);
				atualizaCarrinho(cBo);
				removeu = true;
				break;
			}
			index++;
		}
		return casoProdutoRemovido(removeu);
	}

	private List<CarrinhoBo> getCarrinhos() {
		return ConverterCarrinho.convertListEntityToBo(mDao.getCarrinhos());
	}

	private CarrinhoBo casoProdutoRemovido(boolean removeu) {
		if (removeu) {
			return getCarrinho();
		}
		throw new RuntimeException();

	}

	private void atualizaCarrinho(CarrinhoBo cBo) {
		mDao.updateCarrinho(ConverterCarrinho.convertBoToEntity(cBo));
	}

	private void removeProdutoDosCarrinhos(long id) {
		List<CarrinhoBo> carrinhos = getCarrinhos();
		for (int i = 0; i < carrinhos.size(); i++) {
			CarrinhoBo carrinhoVolta = carrinhos.get(i);
			List<ProdutoBo> produtoBos = carrinhoVolta.getProdutos();
			for (int j = 0; j < produtoBos.size(); j++) {
				ProdutoBo prodBo = produtoBos.get(j);
				if (prodBo.getId() == id) {
					carrinhoVolta.getProdutos().remove(j);
					atualizaCarrinho(carrinhoVolta);
				}
			}
		}
	}
}
