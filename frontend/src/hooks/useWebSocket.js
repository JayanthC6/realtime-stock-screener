import { useEffect, useRef, useState } from 'react'
import { connectWebSocket, disconnectWebSocket } from '../services/websocket'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

export function useWebSocket() {
  const { user } = useAuth()
  const [screenerUpdates, setScreenerUpdates] = useState([])
  const [liveStocks, setLiveStocks] = useState({})
  const clientRef = useRef(null)

  useEffect(() => {
    const onStockUpdate = (data) => {
      setLiveStocks(prev => ({
        ...prev,
        [data.symbol]: data
      }))
    }

    const onScreenerUpdate = (data) => {
      setScreenerUpdates(prev => {
        const exists = prev.findIndex(s => s.symbol === data.symbol)
        if (exists >= 0) {
          const updated = [...prev]
          updated[exists] = data
          return updated
        }
        return [...prev, data]
      })
    }

    const onAlertUpdate = (message) => {
      toast.success(message, { duration: 6000 })
    }

    clientRef.current = connectWebSocket(
      onStockUpdate,
      onScreenerUpdate,
      onAlertUpdate,
      user?.email
    )

    return () => {
      disconnectWebSocket()
    }
  }, [user])

  return { liveStocks, screenerUpdates }
}