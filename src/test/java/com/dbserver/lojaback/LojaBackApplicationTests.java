package com.dbserver.lojaback;

import java.math.BigDecimal;
import java.util.List;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import com.dbserver.lojaback.models.Produto;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.*;


import static io.restassured.RestAssured.given;

class LojaBackApplicationTests{


    @BeforeClass
    public void setup(){
        RestAssured.baseURI = "http://localhost:8080/";
        RestAssured.basePath = "";
    }

    @Test
    public void deveRestornarListaprodutos(){

        Response resposta = given().when().
                get("/produtos").then().
                assertThat().statusCode(HttpStatus.SC_OK).extract().response();

        List<Produto> lista = resposta.jsonPath().getList("", Produto.class);

        Assert.assertEquals(lista.get(0).getId(), Long.valueOf(1), "Id deveria ser 1");
        Assert.assertEquals(lista.get(0).getNome(), "Celular", "Nome do produto deveria ser Celular");
        Assert.assertEquals(lista.size(), 9, "A lista deveria ter tamanho 9");
    }

    @Test
    public void deveRetornarProduto(){
        given().when().get("/produto/{id}", 1).then().log().all().assertThat().statusCode(HttpStatus.SC_OK).extract().response();
    }


    //Todo: Ver se a mensagem é uma regra de negócio.
    @Test
    public void deveRetornarErroProdutoNaoEncontrado(){
        Assert.assertEquals(given().when().get("/produto/{id}", 0).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND).extract().asString(), "O produto informado não foi encontrado.");
    }

    @Test
    public void deveriaCadastrarProduto(){
        Produto produto = new Produto();
        produto.setNome("nome");
        produto.setDescricao("descrição");
        produto.setPrecoUnitario(BigDecimal.valueOf(10));
        produto.setQuantidade(10);

        given().contentType(ContentType.JSON).body(produto).post("/produto").then().assertThat().statusCode(HttpStatus.SC_OK);
    }


    //Ver mensagem incorreta??
    @Test
    public void deveriaRetornarErroNomeObrigatorio(){
        Produto produto = new Produto();
        produto.setDescricao("descrição");
        produto.setPrecoUnitario(BigDecimal.valueOf(10));
        produto.setQuantidade(10);

        String resposta = given().contentType(ContentType.JSON).body(produto).post("/produto").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asString();

        Assert.assertEquals(StringUtils.substringBetween(resposta, "\""), "O nome é obrigatório");
    }

    @Test
    public void deveriaRetornarErroPreçoZerado(){
        Produto produto = new Produto();
        produto.setNome("nome");
        produto.setDescricao("descrição");
        produto.setPrecoUnitario(BigDecimal.valueOf(0));
        produto.setQuantidade(10);

        String resposta = given().contentType(ContentType.JSON).body(produto).post("/produto").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asString();

        Assert.assertEquals(StringUtils.substringBetween(resposta, "\""), "O preço unitário está zerado, informe um valor válido");
    }

    //Mudar mensagem para preços menores que zero.
    @Test
    public void deveriaRetornarErroPreçoMenorQueZero(){
        Produto produto = new Produto();
        produto.setNome("nome");
        produto.setDescricao("descrição");
        produto.setPrecoUnitario(BigDecimal.valueOf(0));
        produto.setQuantidade(10);

        given().contentType(ContentType.JSON).body(produto).post("/produto").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);

        //Assert.assertEquals(given().contentType(ContentType.JSON).body(produto).post("/produto").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asString(), "O preço unitário está zerado, informe um valor válido");
    }

    @Test
    public void deveriaRetornarErroDescricaoCurta(){
        Produto produto = new Produto();
        produto.setNome("nome");
        produto.setDescricao("d");
        produto.setPrecoUnitario(BigDecimal.valueOf(10));
        produto.setQuantidade(10);

        String resposta = given().contentType(ContentType.JSON).body(produto).post("/produto").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asString();

        Assert.assertEquals(StringUtils.substringBetween(resposta, "\""), "A descrição deve ter no mínimo 5 caracteres e no máximo 50 caracteres.");
    }

    @Test
    public void deveriaRetornarErroDescricaoLonga(){
        Produto produto = new Produto();
        produto.setNome("nome");
        produto.setDescricao("Descrição longa demais que claramente ultrapassa 50 caracteres.");
        produto.setPrecoUnitario(BigDecimal.valueOf(10));
        produto.setQuantidade(10);

        given().contentType(ContentType.JSON).body(produto).post("/produto").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);

        //Assert.assertEquals(StringUtils.substringBetween(resposta, "\""), "A descrição deve ter no mínimo 5 caracteres e no máximo 50 caracteres.");    }
    }
    
    //deveria retornar erro para quantidades menores ou iguais a zero?
    @Test
    public void deveriaRetornarErroQuantidadeNegativa(){
        Produto produto = new Produto();
        produto.setNome("nome");
        produto.setDescricao("descrição");
        produto.setPrecoUnitario(BigDecimal.valueOf(10));
        produto.setQuantidade(-10);

        given().contentType(ContentType.JSON).body(produto).post("/produto").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);

        //Assert.assertEquals(StringUtils.substringBetween(resposta, "\""), "A descrição deve ter no mínimo 5 caracteres e no máximo 50 caracteres.");"");
    }

    @Test
    public void deveriaAtualizarProduto(){
        Produto produto = new Produto();
        produto.setId(Long.valueOf(1));
        produto.setNome("Produto atualizado");
        produto.setDescricao("descrição");
        produto.setPrecoUnitario(BigDecimal.valueOf(10));
        produto.setQuantidade(10000);

        given().contentType(ContentType.JSON).body(produto).when().put("/produto").then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void deveriaRetornarErroProdutoNaoExistente() {
        Produto produto = new Produto();
        produto.setId(Long.valueOf(0));
        produto.setNome("Produto atualizado");
        produto.setDescricao("descrição");
        produto.setPrecoUnitario(BigDecimal.valueOf(10));
        produto.setQuantidade(10000);

        given().contentType(ContentType.JSON).body(produto).when().put("/produto").then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void deveriaExcluirProduto(){
        given().when().delete("/produto/{id}", 1).then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void deveriaRetornarErroProdutoNaoExiste(){
        given().when().delete("/produto/{id}", 1).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }











	
}
