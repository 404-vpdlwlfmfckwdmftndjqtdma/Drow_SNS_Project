import type { Channel } from "@/types";

export default function ChannelCard({ channel }: { channel: Channel }) {
  return (
    <article>
      <h4>{channel.name}</h4>
      {/* TODO */}
    </article>
  );
}
