import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import '@/index.css'
import { AppShell } from '@/layouts/AppShell'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false, // Prevents aggressive autosave interactions on window focus
    },
  },
})

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppShell />
    </QueryClientProvider>
  )
}
