/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/*
 * Criado em  13/09/2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.jfrj.siga.vraptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.dp.CpAplicacaoFeriado;
import br.gov.jfrj.siga.dp.CpFeriado;
import br.gov.jfrj.siga.dp.CpLocalidade;
import br.gov.jfrj.siga.dp.CpOcorrenciaFeriado;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.libs.webwork.DpLotacaoSelecao;
import br.gov.jfrj.siga.model.dao.ModeloDao;

@Resource
public class FeriadoController extends SigaController {
	
	public FeriadoController(HttpServletRequest request, Result result, SigaObjects so) {
		super(request, result, CpDao.getInstance(), so);

		result.on(AplicacaoException.class).forwardTo(this).appexception();
		result.on(Exception.class).forwardTo(this).exception();
	}
	
	@Get("/app/feriado/listar")
	public void lista(Integer id) throws Exception {
//		assertAcesso("FE:Ferramentas;CAD_FERIADO: Cadastrar Feriados");
		
		if (id != null){
			CpFeriado feriado = daoFeriado(id);
			result.include("id", feriado.getId());
			result.include("dscFeriado", feriado.getDescricao());
		}			
		
		result.include("itens", CpDao.getInstance().listarCpFeriadoPorDescricao());
	}
	
	@Post("/app/feriado/salvar")
	public void aEditarGravar(String dscFeriado, Integer id) throws Exception {
//		assertAcesso("FE:Ferramentas;CAD_FERIADO: Cadastrar Feriados");

		if (StringUtils.isBlank(dscFeriado)) {
			throw new AplicacaoException("Descri��o do feriado n�o informada");
		}

		CpFeriado feriado = (id == null) 
			? new CpFeriado() 
			: daoFeriado(id);

		feriado.setDscFeriado(dscFeriado);
		
		try {
			ModeloDao.iniciarTransacao();
			dao().gravar(feriado);
			ModeloDao.commitTransacao();
		} catch (final Exception e) {
			ModeloDao.rollbackTransacao();
			throw new AplicacaoException("Erro na grava��o", 0, e);
		}
		
		result.redirectTo(this).lista(null);
	}
	
	@Get("/app/feriado/excluir")
	public void excluirFeriado(Integer id) throws Exception {
//		assertAcesso("FE:Ferramentas;CAD_FERIADO: Cadastrar Feriados");
		
		if (id != null) {
			try {
				ModeloDao.iniciarTransacao();
				CpFeriado feriado = daoFeriado(id);				
				dao().excluir(feriado);				
				ModeloDao.commitTransacao();				
			} catch (final Exception e) {
				ModeloDao.rollbackTransacao();
				throw new AplicacaoException("Erro na exclus�o de Feriado", 0, e);
			}
		} else {
			throw new AplicacaoException("ID n�o informada");
		}
		
		result.redirectTo(this).lista(null);
	}
	
	@Get("/app/feriado/excluir-ocorrencia")
	public void excluirOcorrencia(Integer idOcorrencia) throws Exception {
//		assertAcesso("FE:Ferramentas;CAD_FERIADO: Cadastrar Feriados");
		
		if (idOcorrencia != null) {
			try {
				ModeloDao.iniciarTransacao();
				dao().excluir(daoOcorrenciaFeriado(idOcorrencia));				
				ModeloDao.commitTransacao();				
			} catch (final Exception e) {
				ModeloDao.rollbackTransacao();
				throw new AplicacaoException("Erro na exclus�o de ocorrencia de feriado", 0, e);
			}
		} else {
			throw new AplicacaoException("ID da ocorrencia n�o informada");
		}
		
		result.redirectTo(this).lista(null);
	}
	
	
	
	@Get("/app/feriado/editar-ocorrencia")
	public void editaOcorrencia(Integer id, Integer idOcorrencia) throws Exception {
//		assertAcesso("FE:Ferramentas;CAD_FERIADO: Cadastrar Feriados");

		result.include("orgaosUsu", dao().listarOrgaosUsuarios());
		result.include("listaUF", dao().consultarUF());
		result.include("listaAplicacoes", new ArrayList<>());
		
		if (idOcorrencia != null) {
			CpOcorrenciaFeriado ocorrencia = daoOcorrenciaFeriado(id);	
		
			result.include("id", ocorrencia.getCpFeriado().getIdFeriado());
			result.include("dscFeriado", ocorrencia.getCpFeriado().getDescricao());
			result.include("dtIniFeriado", stringToDate(ocorrencia.getDtRegIniDDMMYY()));
			result.include("dtFimFeriado", stringToDate(ocorrencia.getDtRegFimDDMMYY()));
			result.include("listaAplicacoes", getListaAplicacoes(idOcorrencia));
		} else {
			if (id != null) {
				CpFeriado feriado = daoFeriado(id);
				result.include("id", feriado.getId());
				result.include("dscFeriado", feriado.getDescricao());
			} else {
				throw new AplicacaoException("ID n�o informado");
			}
		}	
	}
	
	@Post("/app/feriado/gravar-ocorrencia")
	public void gravarOcorrencia(Date dtIniFeriado, Date dtFimFeriado, Integer idOcorrencia,
			Integer id, DpLotacaoSelecao lotacao_lotacaoSel, Integer idOrgaoUsu, Integer idLocalidade) throws Exception {
		
//		assertAcesso("FE:Ferramentas;CAD_FERIADO: Cadastrar Feriados");

		DpLotacaoSelecao lotacaoSel = lotacao_lotacaoSel;
		CpAplicacaoFeriado aplicacao = new CpAplicacaoFeriado();
		CpOcorrenciaFeriado ocorrencia = (idOcorrencia == null)
				? new CpOcorrenciaFeriado()
				: daoOcorrenciaFeriado(idOcorrencia);

		if (dtIniFeriado == null) {
			throw new AplicacaoException("Data de in�cio do feriado n�o informada");
		}

		CpFeriado feriado = daoFeriado(id);
		ocorrencia.setCpFeriado(feriado);
		ocorrencia.setDtIniFeriado(dtIniFeriado);
		ocorrencia.setDtFimFeriado(dtFimFeriado);

		if ((lotacaoSel.getId() != null && lotacaoSel.getId() != 0)
				|| (idOrgaoUsu != null && idOrgaoUsu != 0)
				|| (idLocalidade != null && idLocalidade != 0)) {
			
			HashSet<CpAplicacaoFeriado> apls = new HashSet<CpAplicacaoFeriado>();

			if (lotacaoSel.getId() != null && lotacaoSel.getId() != 0) {
				DpLotacao lotacao = dao().consultar(lotacaoSel.getId(), DpLotacao.class, false);
				aplicacao.setDpLotacao(lotacao);
			}

			if (idOrgaoUsu != null && idOrgaoUsu != 0) {
				CpOrgaoUsuario orgao = dao().consultar(idOrgaoUsu, CpOrgaoUsuario.class, false);
				aplicacao.setOrgaoUsu(orgao);
			}

			if (idLocalidade != null && idLocalidade != 0) {
				CpLocalidade localidade = dao().consultar(idLocalidade, CpLocalidade.class, false);
				aplicacao.setLocalidade(localidade);
			}
			
			aplicacao.setCpOcorrenciaFeriado(ocorrencia);
			aplicacao.setFgFeriado(null);

			apls.add(aplicacao);

			ocorrencia.setCpAplicacaoFeriadoSet(apls);
		}

		try {
			ModeloDao.iniciarTransacao();
			dao().gravar(ocorrencia);
			dao().gravar(aplicacao);
			ModeloDao.commitTransacao();
		} catch (final Exception e) {
			ModeloDao.rollbackTransacao();
			throw new AplicacaoException("Erro na grava��o", 0, e);
		}
		
		result.redirectTo(this).lista(null);
	}
	
	
	@Get("/app/feriado/localidades")
	public void listaLocalidades(String nmUF) {
		List<CpLocalidade> localidades = new ArrayList<>();

		if (StringUtils.isBlank(nmUF)) {
			localidades = dao().consultarLocalidades();
		} else {
			localidades = dao().consultarLocalidadesPorUF(nmUF);
		}

		result.include("listaLocalidades", localidades);
	}
	
//	
//	public Integer getId() {
//		return id;
//	}
//
//	public void setId(Integer id) {
//		this.id = id;
//	}	
//	
//
//	public Long getIdOcorrencia() {
//		return idOcorrencia;
//	}
//
//	public void setIdOcorrencia(Long idOcorrencia) {
//		this.idOcorrencia = idOcorrencia;
//	}
//
//	public String getDscFeriado() {
//		return dscFeriado;
//	}
//
//	public void setDscFeriado(String dscFeriado) {
//		this.dscFeriado = dscFeriado;	
//	}
//	
//	public List getItens() {
//		return itens;
//	}
//
//	public void setItens(List feriados) {
//		this.itens = feriados;
//	}	
//
//	public Date getDtIniFeriado() {
//		return dtIniFeriado;
//	}
//
//	public void setDtIniFeriado(Date dtIniFeriado) {
//		this.dtIniFeriado = dtIniFeriado;
//	}
//
//	public Date getDtFimFeriado() {
//		return dtFimFeriado;
//	}
//
//	public void setDtFimFeriado(Date dtFimFeriado) {
//		this.dtFimFeriado = dtFimFeriado;
//	}
//	
//	public DpLotacaoSelecao getLotacaoSel() {
//		return lotacaoSel;
//	}
//
//	public void setLotacaoSel(DpLotacaoSelecao lotacaoSel) {
//		this.lotacaoSel = lotacaoSel;
//	}
//
//	public List getLocalidades() {
//		return localidades;
//	}
//
//	public void setLocalidades(List localidades) {
//		this.localidades = localidades;
//	}
//	
//	public Long getIdLocalidade() {
//		return idLocalidade;
//	}
//
//	public void setIdLocalidade(Long idLocalidade) {
//		this.idLocalidade = idLocalidade;
//	}
//
//		public Long getIdOrgaoUsu() {
//		return idOrgaoUsu;
//	}
//
//	public void setIdOrgaoUsu(Long idOrgaoUsu) {
//		this.idOrgaoUsu = idOrgaoUsu;
//	}
//
//
//	public String getNmUF() {
//		return nmUF;
//	}
//
//	public void setNmUF(String nmUF) {
//		this.nmUF = nmUF;
//	}
//
//	public Long getIdAplicacao() {
//		return idAplicacao;
//	}
//
//	public void setIdAplicacao(Long idAplicacao) {
//		this.idAplicacao = idAplicacao;
//	}

	public CpFeriado daoFeriado(Integer id) {
		return dao().consultar(id, CpFeriado.class, false);
	}
	
	public CpOcorrenciaFeriado daoOcorrenciaFeriado(long id) {
		return dao().consultar(id, CpOcorrenciaFeriado.class, false);
	}
	
//	public CpAplicacaoFeriado daoAplicacaoFeriado(long id) {
//		return dao().consultar(id, CpAplicacaoFeriado.class, false);
//	}

//	public String aExcluirAplicacao() throws Exception {
//		assertAcesso("FE:Ferramentas;CAD_FERIADO: Cadastrar Feriados");
//		if (getIdAplicacao() != null) {
//			try {
//				dao().iniciarTransacao();
//				CpAplicacaoFeriado aplicacao = daoAplicacaoFeriado(getIdAplicacao());				
//				dao().excluir(aplicacao);				
//				dao().commitTransacao();				
//			} catch (final Exception e) {
//				dao().rollbackTransacao();
//				throw new AplicacaoException("Erro na exclus�o de ocorrencia de feriado", 0, e);
//			}
//		} else
//			throw new AplicacaoException("ID da ocorrencia n�o informada");
//
//		return Action.SUCCESS;
//	}
	
	public Date stringToDate(String data) throws Exception {   
        if (data == null || data.equals(""))  
            return null;            
        Date date = null;  
        try {  
            DateFormat strDate = new SimpleDateFormat("dd/MM/yyyy");  
            date = (java.util.Date)strDate.parse(data);  
        } catch (Exception e) {              
            throw e;  
        }  
        return date;  
    }  


//	public String aListarLocalidades() {
//
//			
//		return Action.SUCCESS;
//	}

//	public List<CpUF> getListaUF(){
//		
//		List<CpUF> uf = new ArrayList<CpUF>();
//		uf = dao().consultarUF();
//		
//		return uf;
//		
//	}	
//	
	public List<CpAplicacaoFeriado> getListaAplicacoes(Integer idOcorrencia) {
		List<CpAplicacaoFeriado> aplicacoes = new ArrayList<CpAplicacaoFeriado>();
		CpAplicacaoFeriado apl = new CpAplicacaoFeriado();
		CpOcorrenciaFeriado ocorrencia = new CpOcorrenciaFeriado();
		ocorrencia = dao().consultar(idOcorrencia, CpOcorrenciaFeriado.class, false);
		apl.setCpOcorrenciaFeriado(ocorrencia);
		aplicacoes = dao().listarAplicacoesFeriado(apl);

		return aplicacoes;
	}
}