import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WS_URL = import.meta.env.VITE_WS_URL

let client = null

export const connectWebSocket = (onStockUpdate, onScreenerUpdate, onAlertUpdate, userEmail) => {
  client = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    reconnectDelay: 5000,

    onConnect: () => {
      console.log('WebSocket connected')

      // Subscribe to screener updates
      client.subscribe('/topic/screener', (message) => {
        const data = JSON.parse(message.body)
        onScreenerUpdate(data)
      })

      // Subscribe to all live stock updates
      client.subscribe('/topic/stocks', (message) => {
        const data = JSON.parse(message.body)
        onStockUpdate(data)
      })

      // Subscribe to personal alert notifications
      if (userEmail) {
        client.subscribe(`/user/${userEmail}/queue/alerts`, (message) => {
          onAlertUpdate(message.body)
        })
      }
    },

    onDisconnect: () => {
      console.log('WebSocket disconnected')
    },

    onStompError: (frame) => {
      console.error('WebSocket error:', frame)
    }
  })

  client.activate()
  return client
}

export const subscribeToStock = (symbol, callback) => {
  if (client && client.connected) {
    return client.subscribe(`/topic/stocks/${symbol}`, (message) => {
      const data = JSON.parse(message.body)
      callback(data)
    })
  }
}

export const disconnectWebSocket = () => {
  if (client) {
    client.deactivate()
    client = null
  }
}