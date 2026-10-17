#!/bin/bash
# Script de testes: dispara compras pela API e mostra a resposta do Banco Tranquilo
B=${1:-http://localhost:8080}
compra(){ echo ">> $1"; R=$(curl -s -X POST $B/pagamentos -H 'Content-Type: application/json' -d "$2"); echo "   $R"; echo "$R" | sed -n 's/.*"numeroPedido":"\([^"]*\)".*/\1/p'; }
N1=$(compra "Compra que deve ser APROVADA" '{"nomeCliente":"Ana Souza","emailCliente":"ana@tranquilo.com","itens":[{"produtoId":1,"quantidade":2}]}' | tail -1)
N2=$(compra "Compra RECUSADA (saldo insuficiente)" '{"nomeCliente":"Carla Dias","emailCliente":"carla@tranquilo.com","itens":[{"produtoId":6,"quantidade":2}]}' | tail -1)
sleep 2
echo; echo "== Status apos resposta do Banco Tranquilo =="
curl -s $B/pagamentos/$N1; echo
curl -s $B/pagamentos/$N2; echo
