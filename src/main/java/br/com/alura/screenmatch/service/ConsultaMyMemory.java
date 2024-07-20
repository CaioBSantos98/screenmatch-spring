package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.service.translate.DadosTraducao;

import java.net.URLEncoder;

public class ConsultaMyMemory {
    public static String obterTraducao(String text) {
        ConsumoApi consumoApi = new ConsumoApi();
        ConverteDados conversor = new ConverteDados();

        String texto = URLEncoder.encode(text);
        String langpair = URLEncoder.encode("en|pt-br");

        String url = "https://api.mymemory.translated.net/get?q=" + texto + "&langpair=" + langpair;

        String json = consumoApi.obterDados(url);
        var traducao = conversor.obterDados(json, DadosTraducao.class);

        return traducao.dadosTraducao().textoTraduzido();
    }



}
