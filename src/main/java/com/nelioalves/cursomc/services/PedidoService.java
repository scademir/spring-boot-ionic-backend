package com.nelioalves.cursomc.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nelioalves.cursomc.domain.ItemPedido;
import com.nelioalves.cursomc.domain.PagamentoComBoleto;
import com.nelioalves.cursomc.domain.Pedido;
import com.nelioalves.cursomc.domain.enums.EstadoPagamento;
import com.nelioalves.cursomc.repositories.ItemPedidoRepository;
import com.nelioalves.cursomc.repositories.PagamentoRepository;
import com.nelioalves.cursomc.repositories.PedidoRepository;
import com.nelioalves.cursomc.repositories.ProdutoRepository;
import com.nelioalves.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
	@Autowired
	private PedidoRepository repo;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	public Pedido find(Integer id) {
		Pedido obj = repo.findOne(id);
		if (obj == null) {
			throw new ObjectNotFoundException("Objeto não encontrado! Id: " + id
					+ ", Tipo: " + Pedido.class.getName());
		}
		return obj;
	}
	
	public Pedido insert(Pedido obj) {
		// Instancia Pedido
		obj.setId(null);
		obj.setInstante(new Date());
		
		// Instancia Pagamento
		// Não seta o Id pois o JPA fará isso
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		
		//O Pagamento tem que conhecer o Pedido dele
		obj.getPagamento().setPedido(obj);
		
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			
			//Aqui seta o campo dataVencimento da classe PagamentoComBoleto
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);
		
		// Salva o Pagamento
		pagamentoRepository.save(obj.getPagamento());

		//Percorre todos os itens de pedito
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			
			//Seta o preco pegando da classe Produto 
			ip.setPreco(produtoRepository.findOne(ip.getProduto().getId()).getPreco());
			
			// Associar esse itemPedido com o Pedido que esta inserindo OBJ
			ip.setPedido(obj);
		}
		
		//Salva a Lista de Itens
		itemPedidoRepository.save(obj.getItens());
		
		return obj;
		
	}

}
