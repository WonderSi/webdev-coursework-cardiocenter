export function doctorErrorMessage(cause: unknown, fallback: string): string {
  if (cause instanceof TypeError) return 'Нет соединения с сервером. Попробуйте ещё раз.'
  return cause instanceof Error ? cause.message : fallback
}
