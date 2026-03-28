import { Separator } from 'react-resizable-panels'

interface ResizeHandleProps {
  id?: string
  direction?: 'horizontal' | 'vertical'
  className?: string
}

export function ResizeHandle({ id, direction = 'horizontal', className = '' }: ResizeHandleProps) {
  return (
    <Separator
      id={id?.toString()}
      className={[
        'relative flex items-center justify-center transition-all duration-300 group',
        direction === 'horizontal' ? 'w-[3px] hover:w-[6px] -mx-[1.5px] cursor-col-resize h-full' : 'h-[3px] hover:h-[6px] -my-[1.5px] cursor-row-resize w-full',
        'hover:bg-[var(--color-accent)]/80 z-20',
        className
      ].join(' ')}
    >
      <div 
        className={[
          'transition-all duration-300',
          direction === 'horizontal' 
            ? 'w-full h-full bg-[var(--color-border)] group-hover:bg-transparent shadow-[inset_1px_0_0_rgba(0,0,0,0.05)]' 
            : 'w-full h-full bg-[var(--color-border)] group-hover:bg-transparent shadow-[inset_0_1px_0_rgba(0,0,0,0.05)]'
        ].join(' ')}
      />
      {/* Visual cue indicator (the "grabber") */}
      <div 
        className={[
          'absolute rounded-full bg-[var(--color-accent)] opacity-0 group-hover:opacity-100 transition-all duration-300 shadow-[0_0_8px_var(--color-accent)]',
          direction === 'horizontal' ? 'w-1.5 h-8 left-1/2 -translate-x-1/2' : 'h-1.5 w-8 top-1/2 -translate-y-1/2'
        ].join(' ')}
      />
    </Separator>
  )
}
